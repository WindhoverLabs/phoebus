package com.windhoverlabs.yamcs.core;

import com.windhoverlabs.pv.yamcs.YamcsPV;
import com.windhoverlabs.pv.yamcs.YamcsSubscriptionService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.yamcs.client.EventSubscription;
import org.yamcs.client.YamcsClient;
import org.yamcs.client.archive.ArchiveClient;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.protobuf.CreateEventRequest;
import org.yamcs.protobuf.GetServerInfoResponse;
import org.yamcs.protobuf.GetServerInfoResponse.CommandOptionInfo;
import org.yamcs.protobuf.SubscribeEventsRequest;
// import org.yamcs.protobuf.Event;

public class CMDR_YamcsInstance extends YamcsObject<YamcsObject<?>> {
  public static final Logger logger = Logger.getLogger(CMDR_YamcsInstance.class.getPackageName());
  public static String OBJECT_TYPE = "instance";
  private ProcessorClient yamcsProcessor = null;
  private YamcsSubscriptionService paramSubscriptionService;
  private EventSubscription eventSubscription;
  private ArchiveClient yamcsArchiveClient;
  private CMDR_YamcsInstanceState instanceState;

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
  // TODO:Not sure if we want to have this on every instance and their server...just want it to work
  // for now.
  // Useful for "special" command link arguments such as cop1Bypass
  private HashMap<String, CommandOptionInfo> extraCommandArgs =
      new HashMap<String, CommandOptionInfo>();

  private ObservableList<CommandOption> optionsList = FXCollections.observableArrayList();

  public ObservableList<CommandOption> getOptionsList() {
    return optionsList;
  }

  public HashMap<String, CommandOptionInfo> getExtraCommandArgs() {
    return extraCommandArgs;
  }

  public ArchiveClient getYamcsArchiveClient() {
    return yamcsArchiveClient;
  }

  private ObservableList<CMDR_Event> events = FXCollections.observableArrayList();

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
    //    yamcsClient.listProcessors(OBJECT_TYPE)
  }

  protected void initYamcsSubscriptionService(
      YamcsClient yamcsClient, String serverName, String procesor) {
    paramSubscriptionService =
        new YamcsSubscriptionService(
            yamcsClient.createParameterSubscription(), serverName, this.getName(), procesor);
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
}