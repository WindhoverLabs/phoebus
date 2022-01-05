package com.windhoverlabs.commander.applications.eventlog;

import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.yamcs.client.EventSubscription;
import org.yamcs.client.Page;
import org.yamcs.protobuf.ListEventsRequest;
import org.yamcs.protobuf.SubscribeEventsRequest;
import org.yamcs.protobuf.Yamcs.Event;
import com.windhoverlabs.commander.core.CMDR_YamcsInstance;
import com.windhoverlabs.commander.core.YamcsObject;
import com.windhoverlabs.commander.core.YamcsObjectManager;
import com.windhoverlabs.commander.core.YamcsServer;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import java.util.logging.Level;


public class EventLog {

  private final TableView<Event> tableView;

  TableColumn<Event, String> severityCol = new TableColumn<Event, String>();
  TableColumn<Event, String> annotationCol =  new TableColumn<Event, String>();

  private List<Event> data;
  private final static int dataSize = 10_023;

  private final List<Class<? extends YamcsObject<?>>> itemTypes =
      Arrays.asList(YamcsServer.class, CMDR_YamcsInstance.class);

  private int rowsPerPage = 5;

  // TODO:Eventually these will be in spinner nodes. These are the event filters.
  private String currentServer = "sitl";
  private String currentInstance = "yamcs-cfs";

  private YamcsObject<YamcsServer> root;
  
  private Page<Event> currentPage;

  public EventLog() {
    tableView = new TableView();
    tableView.getColumns().add(severityCol);
    tableView.getColumns().add(annotationCol);
//    data = createData();

    root = YamcsObjectManager.getRoot();
  }
  
  public void nextPage() 
  {
    if(currentPage.hasNextPage()) 
    {   
      currentPage.iterator().forEachRemaining(data::add);
      EventLogInstance.logger.log(Level.WARNING, "Events-->" + data.toString());
    }
  }
  
  private List<Event> createData() {
//    YamcsObjectManager.getServerFromName(currentServer).getInstance(currentinstance)
//        .getYamcsArchiveClient().listEventIndex(Instant, null, null);
    
    ListEventsRequest.newBuilder().setInstance(currentInstance);
    
    EventSubscription eventSubscription = YamcsObjectManager.getServerFromName(currentServer).getYamcsClient().createEventSubscription();
    
    
//     eventSubscription.sendMessage(
//         ListEventsRequest.newBuilder().setInstance(currentinstance).build());
      
    YamcsObjectManager.getServerFromName(currentServer).getInstance(currentInstance)
    .getYamcsArchiveClient().listEvents().whenComplete((page, exc) -> {
      List<Event> eventList = new ArrayList<>();
      currentPage = page;
      page.iterator().forEachRemaining(data::add);
      EventLogInstance.logger.log(Level.WARNING, "Events-->" + data.toString());
      Collections.reverse(eventList); // Output is reverse chronological
      
  });
    

    List<Event> tempData = new ArrayList<>(dataSize);

    for (int i = 0; i < dataSize; i++) {
      // tempData.add(new Event());
    }

    return tempData;
  }

  private TreeItem<YamcsObject<?>> createItem(YamcsObject<?> object) {

    // create tree item with children from game object's list:

    TreeItem<YamcsObject<?>> item = new TreeItem<>(object);
    item.setExpanded(true);
    item.getChildren().addAll(object.getItems().stream().map(this::createItem).collect(toList()));

    // update tree item's children list if game object's list changes:

    object.getItems().addListener((Change<? extends YamcsObject<?>> c) -> {
      while (c.next()) {
        if (c.wasAdded()) {
          item.getChildren()
              .addAll(c.getAddedSubList().stream().map(this::createItem).collect(toList()));
        }
        if (c.wasRemoved()) {
          item.getChildren().removeIf(treeItem -> c.getRemoved().contains(treeItem.getValue()));
        }
      }
    });

    return item;
  }

  private Node createPage(int pageIndex) {

    int fromIndex = pageIndex * rowsPerPage;
    int toIndex = Math.min(fromIndex + rowsPerPage, data.size());
    tableView.setItems(FXCollections.observableArrayList(data.subList(fromIndex, toIndex)));

    return new BorderPane(tableView);
  }

  private PseudoClass asPseudoClass(Class<?> clz) {
    return PseudoClass.getPseudoClass(clz.getSimpleName().toLowerCase());
  }

}
