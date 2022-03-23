package com.windhoverlabs.yamcs.core;

import com.windhoverlabs.pv.yamcs.YamcsPV;
import com.windhoverlabs.pv.yamcs.YamcsSubscriptionService;
import java.time.Instant;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.yamcs.client.EventSubscription;
import org.yamcs.client.YamcsClient;
import org.yamcs.client.archive.ArchiveClient;
import org.yamcs.client.processor.ProcessorClient;
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
  }

  protected void initYamcsSubscriptionService(YamcsClient yamcsClient, String serverName) {
    paramSubscriptionService =
        new YamcsSubscriptionService(
            yamcsClient.createParameterSubscription(), serverName, this.getName());
  }

  protected void initEventSubscription(YamcsClient yamcsClient, String serverName) {
    eventSubscription = yamcsClient.createEventSubscription();
    eventSubscription.addMessageListener(null);
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

  public void activate(YamcsClient yamcsClient, String serverName) {
    initProcessorClient(yamcsClient);
    initYamcsSubscriptionService(yamcsClient, serverName);
    initEventSubscription(yamcsClient, serverName);
    instanceState = CMDR_YamcsInstanceState.ACTIVATED;
  }

  public void deActivate(YamcsClient yamcsClient, String serverName) {
    // TODO:unInit resources...
    instanceState = CMDR_YamcsInstanceState.DEACTIVATED;
  }

  public EventSubscription getEventSubscription() {
    return eventSubscription;
  }

  public void subscribePV(YamcsPV pv) {
    // TODO:Have to let the caller know whether were able to successfully subscribe
    // to this pv or not.
    paramSubscriptionService.register(pv);
  }
}
