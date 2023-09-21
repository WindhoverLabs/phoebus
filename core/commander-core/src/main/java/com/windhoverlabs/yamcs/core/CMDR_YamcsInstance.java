package com.windhoverlabs.yamcs.core;

import com.windhoverlabs.pv.yamcs.YamcsPV;
import com.windhoverlabs.pv.yamcs.YamcsSubscriptionService;
import com.windhoverlabs.yamcs.core.YamcsWebSocketClient.TmStatistics;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.yamcs.TmPacket;
import org.yamcs.client.EventSubscription;
import org.yamcs.client.LinkSubscription;
import org.yamcs.client.MessageListener;
import org.yamcs.client.PacketSubscription;
import org.yamcs.client.Page;
import org.yamcs.client.YamcsClient;
import org.yamcs.client.archive.ArchiveClient;
import org.yamcs.client.mdb.MissionDatabaseClient.ListOptions;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.mdb.ProcessingStatistics;
import org.yamcs.protobuf.CreateEventRequest;
import org.yamcs.protobuf.GetServerInfoResponse;
import org.yamcs.protobuf.GetServerInfoResponse.CommandOptionInfo;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.SubscribeEventsRequest;
import org.yamcs.protobuf.TmPacketData;
// import org.yamcs.protobuf.TmStatistics;
import org.yamcs.protobuf.links.LinkInfo;
import org.yamcs.protobuf.links.SubscribeLinksRequest;
import org.yamcs.utils.TimeEncoding;

// import org.yamcs.protobuf.Event;

public class CMDR_YamcsInstance extends YamcsObject<YamcsObject<?>> {
  public static final Logger logger = Logger.getLogger(CMDR_YamcsInstance.class.getPackageName());
  public static String OBJECT_TYPE = "instance";
  private ProcessorClient yamcsProcessor = null;
  private YamcsSubscriptionService paramSubscriptionService;
  private EventSubscription eventSubscription;
  private LinkSubscription linkSubscription;
  private MissionDatabase missionDatabase;
  //  private EventSubscription eventSubscription;
  private ArchiveClient yamcsArchiveClient;
  private CMDR_YamcsInstanceState instanceState;

  // TODO:Not sure if we want to have this on every instance and their server...just want it to work
  // for now.
  // Useful for "special" command link arguments such as cop1Bypass
  private HashMap<String, CommandOptionInfo> extraCommandArgs =
      new HashMap<String, CommandOptionInfo>();

  private ObservableList<CommandOption> optionsList = FXCollections.observableArrayList();
  private ObservableList<CMDR_Event> events = FXCollections.observableArrayList();
  private ObservableList<LinkInfo> links = FXCollections.observableArrayList();
  private ObservableList<TmStatistics> packets = FXCollections.observableArrayList();

  public ObservableList<TmStatistics> getPackets() {
    return packets;
  }

  private HashMap<String, LinkInfo> linksMap = new HashMap<String, LinkInfo>();

  public HashMap<String, LinkInfo> getLinksMap() {
    return linksMap;
  }

  private HashMap<String, Boolean> activeInLinks = new HashMap<String, Boolean>();

  private HashMap<String, Instant> LastUpdateLinks = new HashMap<String, Instant>();

  public HashMap<String, Boolean> getActiveInLinks() {
    return activeInLinks;
  }

  private HashMap<String, Boolean> activeOutLinks = new HashMap<String, Boolean>();
  private ScheduledThreadPoolExecutor timer;

  public ObservableList<LinkInfo> getLinks() {
    return links;
  }

  public CMDR_YamcsInstanceState getInstanceState() {
    return instanceState;
  }

  // Make this class generic?
  public class CommandOption {
    private String id;
    private String value;

    public CommandOption(String newId, String value) {
      this.id = newId;
      this.value = value;
    }

    public String getValue() {
      return this.value;
    }

    public String getId() {
      return this.id;
    }

    public void setValue(String newValue) {
      this.value = newValue;
    }
  }

  public ObservableList<CommandOption> getOptionsList() {
    return optionsList;
  }

  public HashMap<String, CommandOptionInfo> getExtraCommandArgs() {
    return extraCommandArgs;
  }

  public ArchiveClient getYamcsArchiveClient() {
    return yamcsArchiveClient;
  }

  public ObservableList<CMDR_Event> getEvents() {
    return events;
  }

  public ProcessorClient getYamcsProcessor() {
    return yamcsProcessor;
  }

  public CMDR_YamcsInstance(String name) {
    super(name);
  }

  @Override
  public ObservableList<YamcsObject<?>> getItems() {
    return FXCollections.emptyObservableList();
  }

  @Override
  public void createAndAddChild(String name) {
    throw new IllegalStateException("CMDR_YamcsInstance does not allow child items");
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  protected void initProcessorClient(YamcsClient yamcsClient) {
    yamcsProcessor = yamcsClient.createProcessorClient(getName(), "realtime");
  }

  protected void initYamcsSubscriptionService(
      YamcsClient yamcsClient, String serverName, String procesor) {
    paramSubscriptionService =
        new YamcsSubscriptionService(
            yamcsClient.createParameterSubscription(), serverName, this.getName(), procesor);
  }

  protected void initLinkSubscription(YamcsClient yamcsClient, String serverName) {
    linkSubscription = yamcsClient.createLinkSubscription();
    linkSubscription.addMessageListener(
        linkEvent -> {
          switch (linkEvent.getType()) {
            case REGISTERED:
            case UPDATED:
              {
                var link = linkEvent.getLinkInfo();
                LinkInfo linkFromList = null;

                LastUpdateLinks.put(link.getName(), Instant.now());

                linksMap.put(link.getName(), link);

                boolean linkExistsInlList = false;

                for (var l : links) {
                  if (l != null) {
                    if (l.getName().equals(link.getName())) {
                      linkFromList = l;
                      linkExistsInlList = true;
                    }
                  }
                }

                if (linkExistsInlList) {
                  links.remove(linkFromList);
                }
                links.add(linksMap.get(link.getName()));
              }

              break;
            case UNREGISTERED:
              //               TODO but not currently sent by Yamcs
          }
        });

    linkSubscription.sendMessage(SubscribeLinksRequest.newBuilder().setInstance(getName()).build());
  }

  protected void initEventSubscription(YamcsClient yamcsClient, String serverName) {
    eventSubscription = yamcsClient.createEventSubscription();
    eventSubscription.addMessageListener(
        event -> {
          events.add(
              new CMDR_Event(
                  event.getMessage(),
                  Instant.ofEpochSecond(
                      event.getGenerationTime().getSeconds(), event.getGenerationTime().getNanos()),
                  event.getSeverity(),
                  event.getType(),
                  Instant.ofEpochSecond(
                      event.getReceptionTime().getSeconds(), event.getReceptionTime().getNanos()),
                  event.getSource(),
                  this.getName()));
        });

    yamcsArchiveClient = yamcsClient.createArchiveClient(getName());

    eventSubscription.sendMessage(
        SubscribeEventsRequest.newBuilder().setInstance(getName()).build());
  }

  private MissionDatabase loadMissionDatabase(YamcsClient client) {
    var missionDatabase = new MissionDatabase();

    var mdbClient = client.createMissionDatabaseClient(getName());

    try {
      var page = mdbClient.listParameters(ListOptions.limit(500)).get();
      page.iterator().forEachRemaining(missionDatabase::addParameter);
      while (page.hasNextPage()) {
        page = page.getNextPage().get();
        page.iterator().forEachRemaining(missionDatabase::addParameter);
      }

      var commandPage = mdbClient.listCommands(ListOptions.limit(200)).get();
      commandPage.iterator().forEachRemaining(missionDatabase::addCommand);
      while (commandPage.hasNextPage()) {
        commandPage = commandPage.getNextPage().get();
        commandPage.iterator().forEachRemaining(missionDatabase::addCommand);
      }
    } catch (Exception e) {
      e.printStackTrace();
      //          throw new Exception("Failed to load mission database", e);
    }
    return missionDatabase;
  }

  protected void initMDBParameterRDequest(YamcsClient yamcsClient, String serverName) {
    var mdb = yamcsClient.createMissionDatabaseClient(getName()).listParameters();
    Page<ParameterInfo> paramsPage = null;
    try {
      paramsPage = mdb.get();
    } catch (InterruptedException | ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    var it = paramsPage.iterator();
    it.forEachRemaining(
        p -> {
          //          System.out.println("p-->" + p.getQualifiedName());

          for (var m : p.getType().getMemberList()) {
            //            System.out.println("p member-->" + m.getName());
          }
        });
    while (paramsPage.hasNextPage()) {
      //      var it = paramsPage.iterator();
      try {
        paramsPage = paramsPage.getNextPage().get();
      } catch (InterruptedException | ExecutionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      it = paramsPage.iterator();
      it.forEachRemaining(
          p -> {
            //            System.out.println("p-->" + p.getQualifiedName());

            for (var m : p.getType().getMemberList()) {
              //              System.out.println("p member-->" + m.getName());
            }
          });
    }
  }

  /**
   * Initializes all of the subscriptions to the servers such as event and parameter subscriptions.
   * Always call this AFTER the websocket connection to YAMCS has been established. Ideally inside
   * the connected() method of a org.yamcs.client.ConnectionListener. Otherwise, one might cause a
   * race between the time we "connect" via the websocket and the time we create these
   * subscriptions.
   *
   * @param yamcsClient
   * @param serverName
   */
  // TODO:This shoud return whether or not the instance activated successfully.
  public void activate(YamcsClient yamcsClient, String serverName) {
    initProcessorClient(yamcsClient);
    initYamcsSubscriptionService(yamcsClient, serverName, "realtime");
    initEventSubscription(yamcsClient, serverName);
    initLinkSubscription(yamcsClient, serverName);
    initMDBParameterRDequest(yamcsClient, serverName);
    initTMStats(yamcsClient);

    missionDatabase = loadMissionDatabase(yamcsClient);

    try {
      initCommandOptions(yamcsClient);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    }
    instanceState = CMDR_YamcsInstanceState.ACTIVATED;
  }

  public void subscribeTMStats(YamcsClient yamcsClient, Consumer<ProcessingStatistics> consumer) {
    //    TODO:Don't use the YAMCS thread pool. Use the Java one.
    //    timer = YamcsServer.getServer().getThreadPoolExecutor();
    //
    //    //    Make "realtime configurable"
    //
    //    timer.scheduleAtFixedRate(
    //        () -> {
    //          System.out.println("scheduleAtFixedRate1");
    //          YamcsServer.getServer();
    //          System.out.println("scheduleAtFixedRate2:" + YamcsServer.getServer());
    //          System.out.println("Instance:" + getName());
    //          var instance = YamcsServer.getServer().getInstance(getName());
    //
    //          System.out.println("scheduleAtFixedRate3:" + instance);
    //          YamcsServer.getServer().getInstance(getName()).getProcessor("realtime");
    //
    //          System.out.println("scheduleAtFixedRate4");
    //          ProcessingStatistics ps =
    //              YamcsServer.getServer()
    //                  .getInstance(getName())
    //                  .getProcessor("realtime")
    //                  .getTmProcessor()
    //                  .getStatistics();
    //          //           ps =
    //          //              YamcsServer.getServer()
    //          //                  .getInstance(getName())
    //          //                  .getProcessor("realtime")
    //          //                  .getTmProcessor()
    //          //                  .getStatistics();
    //          System.out.println("scheduleAtFixedRate5:" + ps);
    //          consumer.accept(ps);
    //        },
    //        1,
    //        1,
    //        TimeUnit.SECONDS);
    //	  TODO:Add the API call to YMACS server side
  }

  public void initTMStats(YamcsClient yamcsClient) {
    try {
      new YamcsWebSocketClient(
          stats -> {
            if (stats != null) {
              packets.clear();
              for (TmStatistics s : stats) {
                packets.add(s);
              }
            }
          },
          yamcsClient.getHost(),
          yamcsClient.getPort(),
          getName(),
          yamcsClient.listProcessors(getName()).get().get(0).getName());
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    //
    //    subscribeTMStats(
    //        yamcsClient,
    //        stats -> {
    //          packets.clear();
    //          System.out.println("stats...");
    //          for (var s : stats.snapshot()) {
    //            packets.add(s);
    //          }
    //        });
  }

  private void initPacketSubscription(YamcsClient yamcsClient) {
    PacketSubscription subscription = yamcsClient.createPacketSubscription();
    //    yamcsClient.createProcessorClient(OBJECT_TYPE, OBJECT_TYPE)
    subscription.addMessageListener(
        new MessageListener<TmPacketData>() {

          @Override
          public void onMessage(TmPacketData message) {
            TmPacket pwt =
                new TmPacket(
                    TimeEncoding.fromProtobufTimestamp(message.getReceptionTime()),
                    TimeEncoding.fromProtobufTimestamp(message.getGenerationTime()),
                    message.getSequenceNumber(),
                    message.getPacket().toByteArray());
            //            packetsTable.packetReceived(pwt);
          }

          @Override
          public void onError(Throwable t) {
            //            showError("Error subscribing: " + t.getMessage());
          }
        });

    //    subscription.sendMessage(SubscribeTMStatisticsRequest.newBuilder()
    //            .setInstance(getName())
    ////            .setStream(connectData.streamName)
    //            .build());
  }

  public MissionDatabase getMissionDatabase() {
    return missionDatabase;
  }

  private void initCommandOptions(YamcsClient yamcsClient)
      throws InterruptedException, ExecutionException {
    GetServerInfoResponse info = yamcsClient.getServerInfo().get();
    System.out.println("initCommandOptions-->1");
    for (CommandOptionInfo o : info.getCommandOptionsList()) {
      extraCommandArgs.put(o.getId(), o);

      // Eventually check the type and create Commandoption accordingly
      optionsList.add(new CommandOption(o.getId(), ""));
      System.out.println("initCommandOptions-->2" + optionsList);
    }
    System.out.println("initCommandOptions-->3");
  }

  public void deActivate(YamcsClient yamcsClient, String serverName) {
    // TODO:unInit resources...
    instanceState = CMDR_YamcsInstanceState.DEACTIVATED;
    if (eventSubscription != null) {
      eventSubscription.cancel(true);
      paramSubscriptionService.destroy();
    }
  }

  public EventSubscription getEventSubscription() {
    return eventSubscription;
  }

  public void subscribePV(YamcsPV pv) {
    // TODO:Have to let the caller know whether were able to successfully subscribe
    // to this pv or not.
    paramSubscriptionService.register(pv);
  }

  /** Creates and publishes an event to YAMCS instance. */
  public void publishEvent(String message, YamcsClient yamcsClient) {
    yamcsClient.createEvent(
        CreateEventRequest.newBuilder()
            .setInstance(getName())
            .setMessage(message)
            .setSource("Commander")
            .build());
  }

  public ArrayList<String> getProcessors(YamcsClient yamcsClient) {

    ArrayList<String> processors = new ArrayList<String>();
    try {
      yamcsClient
          .listProcessors(getName())
          .get()
          .forEach(
              p -> {
                processors.add(p.getName());
              });
    } catch (InterruptedException | ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return processors;
  }

  public void switchProcessor(YamcsClient yamcsClient, String serverName, String processorName) {
    //	  This seems redundant....
    paramSubscriptionService.destroy();
    initYamcsSubscriptionService(yamcsClient, serverName, processorName);
  }

  public void getParameters(
      YamcsClient yamcsClient,
      List<String> parameters,
      Instant start,
      Instant end,
      Consumer<ArrayList<Page<ParameterValue>>> consumer) {

    //    this.getYamcsArchiveClient().streamValues(parameters, consumer, start, end);
    ArrayList<Page<ParameterValue>> pages = new ArrayList<Page<ParameterValue>>();
    for (var p : parameters) {
      try {
        pages.add(this.getYamcsArchiveClient().listValues(p, start, end).get());
      } catch (InterruptedException | ExecutionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    consumer.accept(pages);
  }

  public void getParameter(
      YamcsClient yamcsClient,
      String parameter,
      Instant start,
      Instant end,
      Consumer<ArrayList<Page<ParameterValue>>> consumer) {

    //    this.getYamcsArchiveClient().streamValues(parameters, consumer, start, end);
    ArrayList<Page<ParameterValue>> pages = new ArrayList<Page<ParameterValue>>();
    try {
      pages.add(this.getYamcsArchiveClient().listValues(parameter, start, end).get());
    } catch (InterruptedException | ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    consumer.accept(pages);
  }

  public boolean isLinkActive(String linkName) {
    return Duration.between(Instant.now(), LastUpdateLinks.get(linkName)).toMillis() < 1000;
  }
}
