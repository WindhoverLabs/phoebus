package com.windhoverlabs.commander.core;

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

public class CMDR_YamcsInstance extends YamcsObject<YamcsObject<?>> {
  public static final Logger logger = Logger.getLogger(CMDR_YamcsInstance.class.getPackageName());
  public static String OBJECT_TYPE = "instance";
  private ProcessorClient yamcsProcessor = null;
  private YamcsSubscriptionService paramSubscriptionService;
  private EventSubscription eventSubscription;
  private ArchiveClient yamcsArchiveClient;

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
    throw new IllegalStateException("Information has no child items");
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
    eventSubscription.addMessageListener(
        event -> {
          event.getSeverity();
          events.add(
              new CMDR_Event(
                  event.getMessage(),
                  Instant.ofEpochSecond(
                      event.getGenerationTime().getSeconds(), event.getGenerationTime().getNanos()),
                  event.getSeverity()));
        });

    yamcsArchiveClient = yamcsClient.createArchiveClient(getName());

    eventSubscription.sendMessage(
        SubscribeEventsRequest.newBuilder().setInstance(getName()).build());
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
