package org.windhoverlabs.pv.yamcs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.yamcs.client.ParameterSubscription;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.SubscribeParametersRequest;
import org.yamcs.protobuf.SubscribeParametersRequest.Action;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.epics.vtype.VBoolean;
import org.epics.vtype.VDouble;
import org.epics.vtype.VFloat;
import org.epics.vtype.VInt;
import org.epics.vtype.VLong;
import org.epics.vtype.VString;
import org.epics.vtype.VType;
import org.epics.vtype.VUInt;
import org.epics.vtype.VULong;
import org.phoebus.pv.PV;
import org.phoebus.pv.loc.ValueHelper;
import org.windhoverlabs.commander.core.CommanderPlugin;

/**
 * Keeps track of {@link IPV} registration state and takes care of establishing
 * or re-establishing a bundled parameter subscription against Yamcs.
 */
public class YamcsSubscriptionService implements YamcsAware, ParameterSubscription.Listener, CommanderPlugin {

	private static final Logger log = Logger.getLogger(YamcsSubscriptionService.class.getName());

	private Map<NamedObjectId, Set<YamcsPV>> pvsById = new LinkedHashMap<>();

	private ParameterSubscription subscription;
	private AtomicBoolean subscriptionDirty = new AtomicBoolean(false);
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private Set<ParameterValueListener> parameterValueListeners = new HashSet<>();

	public YamcsSubscriptionService(ParameterSubscription newSubscriprion) {

		subscription = newSubscriprion;

		// Periodically check if the subscription needs a refresh
		// (PVs send individual events, so this bundles them)
		executor.scheduleWithFixedDelay(() -> {

			System.out.println("scheduleWithFixedDelay + YamcsSubscriptionService1");
			if (subscriptionDirty.getAndSet(false) && subscription != null) {
				Set<NamedObjectId> ids = getRequestedIdentifiers();
				log.fine(String.format("Modifying subscription to %s", ids));
				subscription.sendMessage(
						SubscribeParametersRequest.newBuilder().setAction(Action.REPLACE).setSendFromCache(true)
								.setAbortOnInvalid(false).setUpdateOnExpiration(true).addAllId(ids).build());
				System.out.println("scheduleWithFixedDelay + YamcsSubscriptionService2");

			}
		}, 500, 500, TimeUnit.MILLISECONDS);

//        YamcsPlugin.addListener(this);
	}

	private Set<NamedObjectId> getRequestedIdentifiers() {
		return pvsById.entrySet().stream().filter(entry -> !entry.getValue().isEmpty()).map(Entry::getKey)
				.collect(Collectors.toSet());
	}

	public boolean isSubscriptionAvailable() {
		return subscription != null;
	}

	public YamcsVType getValue(String pvName) {

//		subscription = YamcsPlugin.getYamcsClient().createParameterSubscription();
//		subscription.addListener(this);

//        // Reset connection and value state
//        pvsById.forEach((id, pvs) -> {
//            pvs.forEach(pv -> {
//            	
//                pv.notifyConnectionChange();
//                pv.notifyValueChange();
//                pv.notifyWritePermissionChange();
//            });
//        });
//
//        // Ready to receive some data
//        Set<NamedObjectId> ids = getRequestedIdentifiers();
//        log.fine(String.format("Subscribing to %s [%s/%s]", ids, instance, processor));
//        subscription.sendMessage(SubscribeParametersRequest.newBuilder()
//                .setInstance(instance)
//                .setProcessor(processor)
//                .setSendFromCache(true)
//                .setAbortOnInvalid(false)
//                .setUpdateOnExpiration(true)
//                .addAllId(ids)
//                .build());
		pvName = getYamcsPvName(pvName);
		System.out.println("getValue of YAMCS param1:" + pvName);
		NamedObjectId id = identityOf(pvName);
		if (subscription != null) {
			System.out.println("getValue of YAMCS param2:" + pvName);

			ParameterValue pval = subscription.get(id);
			System.out.println("getValue of YAMCS param3:" + pvName);

			if (pval != null) {
				System.out.println("getValue of YAMCS param4:" + pvName);

				boolean raw = pvName.startsWith("raw://");
				return (YamcsVType) YamcsVType.fromYamcs(pval, raw);
			}
		}
		System.out.println("getValue of YAMCS param5:" + pvName);
		return null;
	}

	/**
	 * Convert something like yamcs://cfs/CPD/ci/CI_HkTlm_t.usCmdCnt to
	 * /cfs/CPD/ci/CI_HkTlm_t.usCmdCnt. Very useful for querying the Yamcs server.
	 *
	 * @param pvName
	 * @return
	 */
	public static String getYamcsPvName(String pvName) {
		return pvName.substring(7);
	}

	@Override
	public void changeProcessor(String instance, String processor) {
		executor.execute(() -> {
//            if (subscription != null) {
//                subscription.cancel(true);
//                subscription = null;
//                pvsById.forEach((id, pvs) -> {
//                    pvs.forEach(pv -> {
//                        pv.notifyConnectionChange();
//                        pv.notifyValueChange();
//                        pv.notifyWritePermissionChange();
//                    });
//                });
//            }
//
//            if (processor != null) {
//                subscription = YamcsPlugin.getYamcsClient().createParameterSubscription();
//                subscription.addListener(this);
//
//                // Reset connection and value state
//                pvsById.forEach((id, pvs) -> {
//                    pvs.forEach(pv -> {
//                        pv.notifyConnectionChange();
//                        pv.notifyValueChange();
//                        pv.notifyWritePermissionChange();
//                    });
//                });

			// Ready to receive some data
			Set<NamedObjectId> ids = getRequestedIdentifiers();
			log.fine(String.format("Subscribing to %s [%s/%s]", ids, instance, processor));
			subscription.sendMessage(SubscribeParametersRequest.newBuilder().setInstance(instance)
					.setProcessor(processor).setSendFromCache(true).setAbortOnInvalid(false).setUpdateOnExpiration(true)
					.addAllId(ids).build());
		});

	}

	/**
	 * Async adds a Yamcs PV for receiving updates.
	 */
	public void register(YamcsPV pv) {
		NamedObjectId id = identityOf(pv.getName());
		executor.execute(() -> {
			Set<YamcsPV> pvs = pvsById.computeIfAbsent(id, x -> new HashSet<>());
			pvs.add(pv);
			subscriptionDirty.set(true);
		});

	}

//    /**
//     * Async removes a Yamcs PV from receiving updates.
//     */
//    public void unregister(PV pv) {
//        NamedObjectId id = identityOf(pv.getName());
//        executor.execute(() -> {
//            Set<PV> pvs = pvsById.get(id);
//            if (pvs != null) {
//                boolean removed = pvs.remove(pv);
//                if (removed) {
//                    subscriptionDirty.set(true);
//                }
//            }
//        });
//    }

	@Override
	public void destroy() {
		YamcsPlugin.removeListener(this);
		executor.shutdown();
	}

//	@Override
//	public void onData(List<ParameterValue> values) {
//        executor.execute(() -> {
//            for (ParameterValue pval : values) {
//                Set<YamcsPV> pvs = pvsById.get(pval.getId());
//                if (pvs != null) {
//                    pvs.forEach(pv -> pv.notifyListenersOfValue(null));
//                }
//            }
//            parameterValueListeners.forEach(l -> l.onData(values));
//        });
//	}

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

//        if (pvName.startsWith("ops://")) {
//            return NamedObjectId.newBuilder()
//                    .setNamespace("MDB:OPS Name")
//                    .setName(pvName.substring("ops://".length()))
//                    .build();
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
		YamcsVType value = null;

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
		case BOOLEAN:{
			yamcsValues.add(Boolean.toString(parameter.getEngValue().getBooleanValue()));
			valueType = VBoolean.class;
			break;
		}
		case DOUBLE:{
			yamcsValues.add(Double.toString(parameter.getEngValue().getDoubleValue()));
			valueType = VDouble.class;
			break;
		}
		case ENUMERATED:
			// TODO Implement
			break;
		case FLOAT: {
			yamcsValues.add(Float.toString(parameter.getEngValue().getFloatValue()));
			valueType = VFloat.class;
			break;
		}
		case NONE:
			// TODO Implement
			break;
		case SINT32: {
			yamcsValues.add(Integer.toString(parameter.getEngValue().getUint32Value()));
			valueType = VInt.class;
			break;
		}
		case SINT64: {
			yamcsValues.add(Long.toString(parameter.getEngValue().getSint64Value()));
			valueType = VLong.class;
			break;
		}
		case STRING: {
			yamcsValues.add(parameter.getEngValue().getStringValue());
			valueType = VString.class;
			break;
		}
		case TIMESTAMP:
			break;
		case UINT32: {
			yamcsValues.add(Integer.toString(parameter.getEngValue().getUint32Value()));
			valueType = VUInt.class;
			break;
		}
		case UINT64: {
			yamcsValues.add(Long.toString(parameter.getEngValue().getUint64Value()));
			valueType = VULong.class;
			break;
		}
		default:
			break;

		}

		if (!yamcsValues.isEmpty()) {
			try {
				value = (YamcsVType) ValueHelper.getInitialValue(yamcsValues, valueType);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return value;
	}
	
	public void init() {}
//	public void destroy() {}

	@Override
	public void onData(List<ParameterValue> values) {
		// TODO

		for (ParameterValue p : values) {
			try {
				pvsById.get(p).iterator().next().updateValue(getVType(p));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
		}

	}
}
