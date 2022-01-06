package com.windhoverlabs.commander.applications.eventlog;

import com.windhoverlabs.commander.applications.eventlog.EventLog.CMDR_Event;
import com.windhoverlabs.commander.core.YamcsObject;
import com.windhoverlabs.commander.core.YamcsObjectManager;
import com.windhoverlabs.commander.core.YamcsServer;
import java.util.ArrayList;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.yamcs.client.Page;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLog {

  public class CMDR_Event {
    public String message;

    public CMDR_Event(String newMessage) {
      message = newMessage;
    }

    public String toString() {
      return message;
    }
  }

  private final TableView<EventLog.CMDR_Event> tableView = new TableView<EventLog.CMDR_Event>();

  TableColumn<EventLog.CMDR_Event, String> severityCol =
      new TableColumn<EventLog.CMDR_Event, String>();
  TableColumn<EventLog.CMDR_Event, String> annotationCol =
      new TableColumn<EventLog.CMDR_Event, String>();

  TableColumn<EventLog.CMDR_Event, String> messageCol =
      new TableColumn<EventLog.CMDR_Event, String>();

  private ArrayList<EventLog.CMDR_Event> data;
  private static final int dataSize = 10_023;

  private int rowsPerPage = 100;

  // TODO:Eventually these will be in spinner nodes. These are the event filters.
  private String currentServer = "sitl";
  private String currentInstance = "yamcs-cfs";

  private YamcsObject<YamcsServer> root;

  private Page<Event> currentPage;
  private Pagination pagination;
  private SubScene scene;

  private boolean isReady = false;

  public Node getSubScene() {
    return tableView;
  }

  // TODO:Quick hack to run tests
  public void updateEvents() {
    if (isReady) {
      tableView.setItems(FXCollections.observableArrayList(data));
      //      tableView.upd
    }
  }

  public EventLog() {
    EventLogInstance.logger.log(Level.WARNING, "EventLog#1");
    tableView.getColumns().add(messageCol);
    EventLogInstance.logger.log(Level.WARNING, "EventLog#2");
    createData();
    EventLogInstance.logger.log(Level.WARNING, "EventLog#3");
    root = YamcsObjectManager.getRoot();
    //    EventLogInstance.logger.log(Level.WARNING, "EventLog#4");
    //    pagination = new Pagination((data.size() / rowsPerPage + 1), 0);
    //    EventLogInstance.logger.log(Level.WARNING, "EventLog#5");
    //    pagination.setPageFactory(this::createPage);
    //    EventLogInstance.logger.log(Level.WARNING, "EventLog#6");
    //    pagination.setVisible(true);
    //    scene = new SubScene(pagination, 1000, 700);
    EventLogInstance.logger.log(Level.WARNING, "EventLog#7");
    //    Scene scene = new Scene(new BorderPane(pagination), 1024, 768);
  }

  public void nextPage() {
    if (currentPage.hasNextPage()) {
      currentPage
          .iterator()
          .forEachRemaining(
              event -> {
                data.add(new CMDR_Event(event.getMessage()));
              });
      //      EventLogInstance.logger.log(Level.WARNING, "Events-->" + data.toString());
    }
  }

  private void createData() {

    YamcsObjectManager.getServerFromName(currentServer)
        .getInstance(currentInstance)
        .getYamcsArchiveClient()
        .listEvents()
        .whenComplete(
            (page, exc) -> {
              currentPage = page;
              page.iterator()
                  .forEachRemaining(
                      event -> {
                        data.add(new CMDR_Event(event.getMessage()));
                      });

              isReady = true;
              EventLogInstance.logger.log(Level.WARNING, "Events-->" + data.toString());
              //              Collections.reverse(eventList); // Output is reverse chronological
            });
  }

  private Node createPage(int pageIndex) {
    EventLogInstance.logger.log(Level.WARNING, "createPage-->");

    int fromIndex = pageIndex * rowsPerPage;
    int toIndex = Math.min(fromIndex + rowsPerPage, data.size());
    tableView.setItems(FXCollections.observableArrayList(data.subList(fromIndex, toIndex)));

    return new BorderPane(tableView);
  }

  private PseudoClass asPseudoClass(Class<?> clz) {
    return PseudoClass.getPseudoClass(clz.getSimpleName().toLowerCase());
  }
}
