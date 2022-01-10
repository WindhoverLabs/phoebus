package com.windhoverlabs.pv.yamcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.epics.util.array.ArrayDouble;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.AlarmStatus;
import org.epics.vtype.Display;
import org.epics.vtype.EnumDisplay;
import org.epics.vtype.Time;
import org.epics.vtype.VBoolean;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VEnum;
import org.epics.vtype.VFloat;
import org.epics.vtype.VInt;
import org.epics.vtype.VLong;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VTable;
import org.epics.vtype.VType;
import org.epics.vtype.VUInt;
import org.epics.vtype.VULong;
import org.phoebus.pv.PV;
import org.yamcs.client.ParameterSubscription;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.SubscribeParametersRequest;
import org.yamcs.protobuf.SubscribeParametersRequest.Action;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

/**
 * Keeps track of {@link IPV} registration state and takes care of establishing or re-establishing a
 * bundled parameter subscription against Yamcs.
 */
public class YamcsSubscriptionService implements YamcsAware, ParameterSubscription.Listener {

  private static final Logger log = Logger.getLogger(YamcsSubscriptionService.class.getName());

  private static String instanceName;

  private Map<NamedObjectId, Set<YamcsPV>> pvsById = new LinkedHashMap<>();

  private ParameterSubscription subscription;
  private AtomicBoolean subscriptionDirty = new AtomicBoolean(false);
  private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  private Set<ParameterValueListener> parameterValueListeners = new HashSet<>();

  private ArrayList<NamedObjectId> ids = new ArrayList<NamedObjectId>();

  private String serverName = null;

  static final Alarm UDF = Alarm.of(AlarmSeverity.UNDEFINED, AlarmStatus.UNDEFINED, "UDF");

  public YamcsSubscriptionService(
      ParameterSubscription newSubscriprion, String newServerName, String newInstanceName) {
    serverName = newServerName;
    subscription = newSubscriprion;
    instanceName = newInstanceName;
    subscription.addListener(this);

    // Periodically check if the subscription needs a refresh
    // (PVs send individual events, so this bundles them)
    executor.scheduleWithFixedDelay(
        () -> {
          if (subscriptionDirty.getAndSet(false) && subscription != null) {
            Set<NamedObjectId> ids = getRequestedIdentifiers();
            log.fine(String.format("Modifying subscription to %s", ids));
            subscription.sendMessage(
                SubscribeParametersRequest.newBuilder()
                    .setAction(Action.REPLACE)
                    .setSendFromCache(true)
                    .setAbortOnInvalid(false)
                    .setUpdateOnExpiration(true)
                    .addAllId(ids)
                    .build());
          }
        },
        500,
        500,
        TimeUnit.MILLISECONDS);
  }

  private Set<NamedObjectId> getRequestedIdentifiers() {
    return pvsById.entrySet().stream()
        .filter(entry -> !entry.getValue().isEmpty())
        .map(Entry::getKey)
        .collect(Collectors.toSet());
  }

  public boolean isSubscriptionAvailable() {
    return subscription != null;
  }

  /**
   * Convert something like yamcs://cfs/CPD/ci/CI_HkTlm_t.usCmdCnt to
   * /cfs/CPD/ci/CI_HkTlm_t.usCmdCnt. Very useful for querying the Yamcs server.
   *
   * @param pvName
   * @return
   */
  public static String getYamcsPvName(String pvName, String serverName) {
    String subStr = "//" + serverName + ":" + instanceName;

    return pvName.substring(subStr.length());
  }

  @Override
  public void changeProcessor(String instance, String processor) {
    executor.execute(
        () -> {
          // Ready to receive some data
          Set<NamedObjectId> ids = getRequestedIdentifiers();
          log.fine(String.format("Subscribing to %s [%s/%s]", ids, instance, processor));
          subscription.sendMessage(
              SubscribeParametersRequest.newBuilder()
                  .setInstance(instance)
                  .setProcessor(processor)
                  .setSendFromCache(true)
                  .setAbortOnInvalid(false)
                  .setUpdateOnExpiration(true)
                  .addAllId(ids)
                  .build());
        });
  }

  /** Async adds a Yamcs PV for receiving updates. */
  public void register(YamcsPV pv) {
    NamedObjectId id =
        YamcsSubscriptionService.identityOf(
            YamcsSubscriptionService.getYamcsPvName(pv.getName(), serverName));
    executor.execute(
        () -> {
          Set<YamcsPV> pvs = pvsById.computeIfAbsent(id, x -> new HashSet<>());
          pvs.add(pv);
          subscriptionDirty.set(true);
        });

    ids.add(id);

    try {
      subscription.sendMessage(
          SubscribeParametersRequest.newBuilder()
              .setInstance(instanceName)
              .setProcessor("realtime")
              .setSendFromCache(true)
              .setAbortOnInvalid(false)
              .setUpdateOnExpiration(true)
              .addId(id)
              .setAction(Action.ADD)
              .build());
    } catch (Exception e) {
      System.out.println("e:" + e);
    }
  }

  /** Async removes a Yamcs PV from receiving updates. */
  public void unregister(PV pv) {
    NamedObjectId id = identityOf(pv.getName());
    executor.execute(
        () -> {
          Set<YamcsPV> pvs = pvsById.get(id);
          if (pvs != null) {
            boolean removed = pvs.remove(pv);
            if (removed) {
              subscriptionDirty.set(true);
            }
          }
        });
  }

  public void destroy() {
    YamcsPlugin.removeListener(this);
    executor.shutdown();
  }

  public void addParameterValueListener(ParameterValueListener listener) {
    parameterValueListeners.add(listener);
  }

  @Override
  public void onInvalidIdentification(NamedObjectId id) {
    //        executor.execute(() -> {
    //            // We keep the id in pvsById, we want to again receive the invalid
    //            // identification when the subscription is updated.
    //            Set<PV> pvs = pvsById.get(id);
    //            if (pvs != null) {
    //                pvs.forEach(PV::setInvalid);
    //            }
    //        });
  }

  public static NamedObjectId identityOf(String pvName) {
    return NamedObjectId.newBuilder().setName(pvName).build();
    //    if (pvName.startsWith("yamcs://")) {
    //      return
    // NamedObjectId.newBuilder().setName(pvName.substring("yamcs://".length())).build();
    //    } else {
    //      System.out.println("identityOf2" + pvName);
    //    }
    //        } else if (pvName.startsWith("para://")) {
    //            return NamedObjectId.newBuilder()
    //                    .setName(pvName.substring("para://".length()))
    //                    .build();
    //        } else if (pvName.startsWith("raw://")) {
    //            return NamedObjectId.newBuilder()
    //                    .setName(pvName.substring("raw://".length()))
    //                    .build();
    //        } else {
    //            return NamedObjectId.newBuilder()
    //                    .setName(pvName)
    //                    .build();
    //        }
  }

  @FunctionalInterface
  public static interface ParameterValueListener {
    void onData(List<ParameterValue> values);
  }

  /**
   * create a VType from a yamcs ParameterValue object.
   *
   * @param parameter
   * @return
   */
  private VType getVType(ParameterValue parameter) {
    ArrayList<String> yamcsValues = new ArrayList<String>();

    Class<? extends VType> valueType = null;
    VType value = null;

    switch (parameter.getEngValue().getType()) {
      case AGGREGATE:
        // TODO Implement
        break;
      case ARRAY:
        // TODO Implement
        break;
      case BINARY:
        // TODO Implement
        break;
      case BOOLEAN:
        {
          yamcsValues.add(Boolean.toString(parameter.getEngValue().getBooleanValue()));
          valueType = VBoolean.class;
          break;
        }
      case DOUBLE:
        {
          yamcsValues.add(Double.toString(parameter.getEngValue().getDoubleValue()));
          valueType = VDouble.class;
          break;
        }
        //		case ENUMERATED:
        ////			yamcsValues.add(Enum.toString(parameter.getEngValue().getS));
        //			valueType = VFloat.class;
        //			break;
      case FLOAT:
        {
          yamcsValues.add(Float.toString(parameter.getEngValue().getFloatValue()));
          valueType = VFloat.class;
          break;
        }
      case NONE:
        // TODO Implement
        break;
      case SINT32:
        {
          yamcsValues.add(Integer.toString(parameter.getEngValue().getUint32Value()));
          valueType = VInt.class;
          break;
        }
      case SINT64:
        {

          //            return
          // Value.newBuilder().setType(Type.STRING).setStringValue(String.valueOf(value)).build();
          yamcsValues.add(Long.toString(parameter.getEngValue().getSint64Value()));
          valueType = VLong.class;
          break;
        }
      case STRING:
      case ENUMERATED:
        {
          yamcsValues.add(parameter.getEngValue().getStringValue());
          valueType = VString.class;
          break;
        }
      case TIMESTAMP:
        break;
      case UINT32:
        {
          yamcsValues.add(Integer.toString(parameter.getEngValue().getUint32Value()));
          valueType = VUInt.class;
          break;
        }
      case UINT64:
        {
          yamcsValues.add(Long.toString(parameter.getEngValue().getUint64Value()));
          valueType = VULong.class;
          break;
        }
      default:
        break;
    }

    if (!yamcsValues.isEmpty()) {
      try {
        value = getInitialValue(yamcsValues, valueType);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return value;
  }

  /**
   * @param items Items from <code>splitInitialItems</code>
   * @return All items as strings, surrounding quotes removed, un-escaping quotes
   */
  private static List<String> getInitialStrings(List<String> items) {
    if (items == null) return Arrays.asList("");
    final List<String> strings = new ArrayList<>(items.size());
    for (String item : items)
      if (item.startsWith("\""))
        strings.add(item.substring(1, item.length() - 1).replace("\\\"", "\""));
      else strings.add(item);
    return strings;
  }

  /**
   * @param items Items from <code>splitInitialItems</code>
   * @return Numeric values for all items
   * @throws Exception on error
   */
  public static double[] getInitialDoubles(List<?> items) throws Exception {
    final double[] values = new double[items.size()];
    for (int i = 0; i < values.length; ++i) {
      try {
        final String text = Objects.toString(items.get(i));
        if (text.startsWith("0x")) values[i] = Integer.parseInt(text.substring(2), 16);
        else values[i] = Double.parseDouble(text);
      } catch (NumberFormatException ex) {
        throw new Exception("Cannot parse number from " + items.get(i));
      }
    }

    return values;
  }

  /**
   * @param items Items from <code>splitInitialItems</code>
   * @return Boolean list of all items
   */
  private static List<Boolean> getInitialBooleans(List<String> items) {
    if (items == null) return Arrays.asList(Boolean.FALSE);
    return items.stream()
        .map(
            item -> {
              return Boolean.parseBoolean(item);
            })
        .collect(Collectors.toList());
  }

  /**
   * @param items Items from <code>splitInitialItems</code>, i.e. strings are quoted
   * @param type Desired VType
   * @return VType for initial value
   * @throws Exception on error
   */
  public static VType getInitialValue(final List<String> items, Class<? extends VType> type)
      throws Exception {
    if (type == VDouble.class) {
      if (items == null) return VDouble.of(0.0, UDF, Time.now(), Display.none());
      if (items.size() == 1)
        return VDouble.of(getInitialDoubles(items)[0], Alarm.none(), Time.now(), Display.none());
      else throw new Exception("Expected one number, got " + items);
    }

    if (type == VFloat.class) {
      if (items == null) return VFloat.of(0.0, UDF, Time.now(), Display.none());
      if (items.size() == 1)
        return VFloat.of(getInitialDoubles(items)[0], Alarm.none(), Time.now(), Display.none());
      else throw new Exception("Expected one number, got " + items);
    }

    if (type == VLong.class) {
      if (items.size() == 1)
        return VLong.of(
            (long) getInitialDoubles(items)[0], Alarm.none(), Time.now(), Display.none());
      else throw new Exception("Expected one number, got " + items);
    }

    if (type == VInt.class) {
      if (items.size() == 1)
        return VInt.of(
            (long) getInitialDoubles(items)[0], Alarm.none(), Time.now(), Display.none());
      else throw new Exception("Expected one number, got " + items);
    }

    if (type == VUInt.class) {
      if (items.size() == 1)
        return VInt.of(
            (long) getInitialDoubles(items)[0], Alarm.none(), Time.now(), Display.none());
      else throw new Exception("Expected one number, got " + items);
    }

    if (type == VBoolean.class) {
      if (items == null || items.size() == 1)
        return VBoolean.of(getInitialBooleans(items).get(0), Alarm.none(), Time.now());
      else throw new Exception("Expected one boolean, got " + items);
    }

    if (type == VString.class) {
      if (items == null || items.size() == 1)
        return VString.of(getInitialStrings(items).get(0), Alarm.none(), Time.now());
      else throw new Exception("Expected one string, got " + items);
    }

    if (type == VDoubleArray.class)
      return VDoubleArray.of(
          ArrayDouble.of(getInitialDoubles(items)), Alarm.none(), Time.now(), Display.none());

    //        if (type == VBooleanArray.class)
    //            return VBooleanArray.of(ArrayBoolean.of(getInitialBooleans(items)), Alarm.none(),
    // Time.now());

    if (type == VStringArray.class)
      return VStringArray.of(getInitialStrings(items), Alarm.none(), Time.now());

    if (type == VEnum.class) {
      if (items.size() < 2) throw new Exception("VEnum needs at least '(index, \"Label0\")'");
      final int initial;
      try {
        initial = Integer.parseInt(items.get(0));
      } catch (NumberFormatException ex) {
        throw new Exception("Cannot parse enum index", ex);
      }
      // Preserve original list
      final List<String> copy = new ArrayList<>(items.size() - 1);
      for (int i = 1; i < items.size(); ++i) copy.add(items.get(i));
      final List<String> labels = getInitialStrings(copy);
      return VEnum.of(initial, EnumDisplay.of(labels), Alarm.none(), Time.now());
    }

    if (type == VTable.class) {
      final List<String> headers = getInitialStrings(items);
      final List<Class<?>> types = new ArrayList<>();
      final List<Object> values = new ArrayList<>();
      while (headers.size() > values.size()) { // Assume each column is of type string, no values
        types.add(String.class);
        values.add(Collections.emptyList());
      }
      return VTable.of(types, headers, values);
    }
    throw new Exception("Cannot obtain type " + type.getSimpleName() + " from " + items);
  }

  @Override
  public void onData(List<ParameterValue> values) {
    // TODO
    for (ParameterValue p : values) {
      try {
        pvsById.get(p.getId()).iterator().next().updateValue(getVType(p));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      ;
    }
  }
}
