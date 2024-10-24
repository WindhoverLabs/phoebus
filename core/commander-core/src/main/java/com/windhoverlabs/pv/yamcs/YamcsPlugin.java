package com.windhoverlabs.pv.yamcs;

import com.windhoverlabs.yamcs.core.MissionDatabase;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.yamcs.client.ClearanceSubscription;
import org.yamcs.client.ClientException;
import org.yamcs.client.ConnectionListener;
import org.yamcs.client.ProcessorSubscription;
import org.yamcs.client.TimeSubscription;
import org.yamcs.client.YamcsClient;
import org.yamcs.client.archive.ArchiveClient;
import org.yamcs.client.mdb.MissionDatabaseClient;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.client.storage.StorageClient;
import org.yamcs.protobuf.ClearanceInfo;
import org.yamcs.protobuf.GetServerInfoResponse;
import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.SubscribeTimeRequest;
import org.yamcs.protobuf.UserInfo;

/**
 * Singleton container for yamcs server utilities.
 *
 * @author lgomez
 */
public class YamcsPlugin {
  private static final Logger log = Logger.getLogger(YamcsPlugin.class.getName());
  private static final AtomicInteger COMMAND_SEQUENCE = new AtomicInteger(1);

  private SimpleDateFormat format;
  private SimpleDateFormat tzFormat;

  private YamcsClient yamcsClient;

  private String instance;
  private ProcessorInfo processor;
  private MissionDatabase missionDatabase;
  private GetServerInfoResponse serverInfo;
  private UserInfo userInfo;

  private TimeSubscription timeSubscription;
  private ClearanceSubscription clearanceSubscription;
  private ProcessorSubscription processorSubscription;

  private static YamcsPlugin plugin = new YamcsPlugin();

  private static final ConnectionListener DISCONNECT_NOTIFIER =
      new ConnectionListener() {

        @Override
        public void connected() {
          // Already handled by YamcsConnector
        }

        @Override
        public void connecting() {
          // Already handled by YamcsConnector
        }

        @Override
        public void disconnected() {
          if (plugin == null) {
            // Plugin is shutting down
            // Prevent downstream exceptions
            return;
          }
          disconnect(true /* lost */);
        }

        @Override
        public void connectionFailed(Throwable cause) {
          // TODO Auto-generated method stub

        }
      };

  private Set<YamcsAware> listeners = new CopyOnWriteArraySet<>();

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  private List<PluginService> pluginServices = new CopyOnWriteArrayList<>();

  public void start() {
    plugin = this;
    // Warning to future self: don't access preference store here. It triggers
    // before workspace selection, causing
    // chaos.

    //		pluginServices.add(new YamcsSubscriptionService());
  }

  public void init(String url, int port) {
    connect(url, port);
    //		TODO:Eventually all of this should be configurable
    instance = "yamcs-cfs";
    processor = ProcessorInfo.newBuilder().setName("realtime").build();
  }

  public static void addListener(YamcsAware listener) {
    plugin.listeners.add(listener);

    if (plugin.yamcsClient != null) {
      listener.onYamcsConnected();
    }
    if (plugin.instance != null) {
      listener.changeInstance(plugin.instance);
    }
    if (plugin.processor != null) {
      listener.changeProcessor(plugin.instance, plugin.processor.getName());
      listener.changeProcessorInfo(plugin.processor);
    }
    if (plugin.clearanceSubscription != null) {
      boolean enabled = plugin.processor != null && plugin.processor.getCheckCommandClearance();
      ClearanceInfo clearanceInfo = plugin.clearanceSubscription.getCurrent();
      if (clearanceInfo != null && clearanceInfo.hasLevel()) {
        listener.updateClearance(enabled, clearanceInfo.getLevel());
      } else {
        listener.updateClearance(enabled, null);
      }
    }
  }

  public static void removeListener(YamcsAware listener) {
    plugin.listeners.remove(listener);
  }

  @SuppressWarnings("unchecked")
  public static <T extends PluginService> T getService(Class<T> clazz) {
    for (PluginService service : plugin.pluginServices) {
      if (service.getClass().isAssignableFrom(clazz)) {
        return (T) service;
      }
    }
    return null;
  }

  public static UserInfo getUser() {
    return plugin.userInfo;
  }

  public static GetServerInfoResponse getServerInfo() {
    return plugin.serverInfo;
  }

  private static boolean isSuperuser() {
    UserInfo userInfo = getUser();
    return userInfo != null && (userInfo.hasSuperuser() && userInfo.getSuperuser());
  }

  public static boolean hasSystemPrivilege(String systemPrivilege) {
    if (!isAuthorizationEnabled()) {
      return true;
    }
    if (getUser() == null) {
      return false;
    }
    return false;

    //		TODO: Build system does not include getSystemPrivilegeList for some reason
    //		return isSuperuser() || getUser().getSystemPrivilegeList().contains(systemPrivilege);
  }

  public static boolean isAuthorizationEnabled() {
    UserInfo userInfo = getUser();
    return userInfo != null ? !userInfo.getSuperuser() : false;
  }

  public void stop() {
    for (PluginService pluginService : pluginServices) {
      pluginService.dispose();
    }

    //		if (PVFactory.SIMPLE_PV_THREAD != null) {
    //			PVFactory.SIMPLE_PV_THREAD.shutdown();
    //		}
    try {
      plugin = null;
      if (yamcsClient != null) {
        yamcsClient.close();
        yamcsClient = null;
      }
      if (executor != null) {
        executor.shutdownNow();
      }
    } finally {
      //			super.stop(context);
    }
  }

  public static YamcsPlugin getDefault() {
    return plugin;
  }

  public void setDateFormat(String pattern) {
    format = new SimpleDateFormat(pattern, Locale.US);
    tzFormat = new SimpleDateFormat(pattern + " Z", Locale.US);
  }

  /**
   * Formats a Yamcs instant. Timezone information is not added. Must be called on SWT thread due to
   * reuse of dateformatter.
   */
  public String formatInstant(Instant instant) {
    return formatInstant(instant, false);
  }

  /**
   * Formats a Yamcs instant. Must be called on SWT thread due to reuse of dateformatter.
   *
   * @param tzOffset whether timezone offset is added to the output string.
   */
  public String formatInstant(Instant instant, boolean tzOffset) {
    ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, YamcsPlugin.getZoneId());
    Calendar cal = GregorianCalendar.from(zdt);
    cal.setTimeZone(YamcsPlugin.getTimeZone());
    if (tzOffset) {
      tzFormat.setTimeZone(cal.getTimeZone());
      return tzFormat.format(cal.getTime());
    } else {
      format.setTimeZone(cal.getTimeZone());
      return format.format(cal.getTime());
    }
  }

  public static YamcsClient getYamcsClient() {
    return plugin.yamcsClient;
  }

  public static ArchiveClient getArchiveClient() {
    YamcsClient client = getYamcsClient();
    if (client != null) {
      return client.createArchiveClient(plugin.instance);
    }
    return null;
  }

  public static StorageClient getStorageClient() {
    YamcsClient client = getYamcsClient();
    if (client != null) {
      return client.createStorageClient();
    }
    return null;
  }

  public static ProcessorClient getProcessorClient() {
    YamcsClient client = getYamcsClient();
    if (client != null) {
      return client.createProcessorClient(plugin.instance, plugin.processor.getName());
    }
    return null;
  }

  public static MissionDatabaseClient getMissionDatabaseClient() {
    YamcsClient client = getYamcsClient();
    if (client != null) {
      return client.createMissionDatabaseClient(plugin.instance);
    }
    return null;
  }

  public static String getInstance() {
    return plugin != null ? plugin.instance : null;
  }

  public static String getProcessor() {
    ProcessorInfo info = getProcessorInfo();
    return info != null ? info.getName() : null;
  }

  public static ProcessorInfo getProcessorInfo() {
    return plugin != null ? plugin.processor : null;
  }

  public static SignificanceLevelType getClearance() {
    ClearanceInfo info = plugin.clearanceSubscription.getCurrent();
    return info.hasLevel() ? info.getLevel() : null;
  }

  public static MissionDatabase getMissionDatabase() {
    return plugin.missionDatabase;
  }

  public static Instant getMissionTime() {
    return getMissionTime(false);
  }

  public static Instant getMissionTime(boolean wallClockIfUnset) {
    Instant t = plugin.timeSubscription != null ? plugin.timeSubscription.getCurrent() : null;
    if (wallClockIfUnset && t == null) {
      t = Instant.now();
    }
    return t;
  }

  public static TimeZone getTimeZone() {
    // Currently always using local timezone, because need to hack into XYChart
    // because
    // it doesn't support Timezones. Only date formats seem to be accounted for.
    // At least for now, it should stay consistent with the workbench
    // TODO Research modifications to SWT xychart and then make this controllable
    // from user prefs
    return TimeZone.getDefault();
  }

  public static ZoneId getZoneId() {
    return ZoneId.systemDefault();
  }

  public static int nextCommandSequenceNumber() {
    return COMMAND_SEQUENCE.incrementAndGet();
  }

  public static Iterator<YamcsAware> listeners() {
    return plugin.listeners.iterator();
  }

  private static void setupGlobalTimeSubscription() {
    if (plugin.instance != null) {
      plugin.timeSubscription = getYamcsClient().createTimeSubscription();
      //			plugin.timeSubscription.addMessageListener(proto -> {
      //				Instant instant = Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos());
      //				plugin.listeners.forEach(l -> l.updateTime(instant));
      //			});
      SubscribeTimeRequest.Builder requestb =
          SubscribeTimeRequest.newBuilder().setInstance(plugin.instance);
      if (plugin.processor != null) {
        requestb.setProcessor(plugin.processor.getName());
      }
      //			plugin.timeSubscription.sendMessage(requestb.build());
    }
  }

  public static void disconnect(boolean lost) {
    if (plugin.yamcsClient != null) {
      if (!lost) {
        log.info(
            "Disconnecting from "
                + plugin.yamcsClient.getHost()
                + ":"
                + plugin.yamcsClient.getPort());
      }

      // Ensure we don't get an async callback when closing the client.
      // It could mess up a shortly scheduled connection attempt.
      // (use case: connecting to another configuration while already connected)
      plugin.yamcsClient.removeConnectionListener(DISCONNECT_NOTIFIER);

      plugin.yamcsClient.close();
      plugin.yamcsClient = null;

      // We control this notification from here, instead of from
      // the websocket disconnect callback, because we don't want
      // the external roundtrip dependency, when disconnecting
      // and another connection is immediately tried.
      //
      // Probably YamcsClient this complication would be better
      // relocated directly in YamcsClient (locally call disconnect,
      // when the client is manually closed).
      log.fine("Notify downstream components of Studio disconnect");
      for (YamcsAware l : plugin.listeners) {
        log.fine(String.format(" -> Inform %s", l.getClass().getSimpleName()));
        l.onYamcsDisconnected();
      }
    }

    plugin.serverInfo = null;
    plugin.userInfo = null;
    plugin.instance = null;
    plugin.processor = null;
    plugin.missionDatabase = null;

    plugin.timeSubscription = null;
    plugin.clearanceSubscription = null;

    plugin.listeners.forEach(
        listener -> {
          listener.updateClearance(false, null);
          listener.changeInstance(null);
          listener.changeProcessor(null, null);
          listener.changeProcessorInfo(null);
          listener.updateTime(null);
        });
  }

  public static YamcsPlugin getPlugin() {
    return plugin;
  }

  /**
   * Creates a new YamcsClient and establishes a new connection with Yamcs server. Any previously
   * active client connections are closed, if there are any.
   *
   * @param url
   * @param port
   */
  private void connect(String url, int port) {
    if (yamcsClient != null) {
      yamcsClient.close();
    }
    System.out.println("yamcs connect1");
    yamcsClient = YamcsClient.newBuilder(url, port).build();
    System.out.println("yamcs connect2");

    try {
      System.out.println("yamcs connect3");

      yamcsClient.connectWebSocket();
      System.out.println("yamcs connect4");

    } catch (ClientException e) {
      // TODO Auto-generated catch block
      System.out.println("yamcs connect5");
      e.printStackTrace();
    }
    System.out.println("yamcs connect6");
  }
}
