package com.windhoverlabs.commander.applications.eventlog;

import com.windhoverlabs.commander.applications.eventlog.EventLog.CMDR_Event;
import com.windhoverlabs.commander.core.YamcsObject;
import com.windhoverlabs.commander.core.YamcsObjectManager;
import com.windhoverlabs.commander.core.YamcsServer;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.yamcs.client.Page;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLog {

  private static final int MAX_PAGES = 16;

  public class CMDR_Event {
    public SimpleStringProperty message;

    public CMDR_Event(String newMessage) {
      message = new SimpleStringProperty(newMessage);
    }

    public String toString() {
      return message.getValue();
    }
  }

  private final TableView<EventLog.CMDR_Event> tableView = new TableView<EventLog.CMDR_Event>();

  TableColumn<EventLog.CMDR_Event, String> severityCol =
      new TableColumn<EventLog.CMDR_Event, String>();
  TableColumn<EventLog.CMDR_Event, String> annotationCol =
      new TableColumn<EventLog.CMDR_Event, String>();

  TableColumn<EventLog.CMDR_Event, String> messageCol =
      new TableColumn<EventLog.CMDR_Event, String>("Message");

  private ArrayList<EventLog.CMDR_Event> data = new ArrayList<EventLog.CMDR_Event>();
  private static final int dataSize = 10_023;

  private int rowsPerPage = 100;

  // TODO:Eventually these will be in spinner nodes. These are the event filters.
  private String currentServer = "sitl";
  private String currentInstance = "yamcs-cfs";

  private YamcsObject<YamcsServer> root;

  private Page<Event> currentPage;
  private Pagination pagination;
  private SubScene scene;
  private Node rootPane;

  private boolean isReady = false;

  public Node getRootPane() {
    return rootPane;
  }

  // TODO:Quick hack to run tests
  public void updateEvents() {
    if (isReady) {
      //      EventLogInstance.logger.log(Level.WARNING, "isReady-->" + data.toString());
      //      tableView.setItems(FXCollections.observableArrayList(data));
      //      tableView.upd
    }
  }

  public EventLog() {
    messageCol.setCellValueFactory(event -> event.getValue().message);
    tableView.getColumns().add(messageCol);
    createData();
    root = YamcsObjectManager.getRoot();
    //    pagination = new Pagination((data.size() / rowsPerPage + 1), 0);
    pagination = new Pagination(MAX_PAGES, 0);

    pagination.setPageFactory(this::createPage);
    pagination.setVisible(true);
    rootPane = new BorderPane(pagination);
  }

  public void nextPage() {
    if (currentPage.hasNextPage()) {
      currentPage.getNextPage().whenComplete((page, exec) -> {});

      currentPage
          .iterator()
          .forEachRemaining(
              event -> {
                data.add(new CMDR_Event(event.getMessage()));
              });
    }
  }

  private void createData() {
    try {
      currentPage =
          YamcsObjectManager.getServerFromName(currentServer)
              .getInstance(currentInstance)
              .getYamcsArchiveClient()
              .listEvents()
              .get();

      currentPage
          .iterator()
          .forEachRemaining(
              event -> {
                data.add(new CMDR_Event(event.getMessage()));
              });

      for (int page = 1; page < MAX_PAGES && currentPage.hasNextPage(); page++) {
        currentPage = currentPage.getNextPage().get();
        currentPage
            .iterator()
            .forEachRemaining(
                event -> {
                  data.add(new CMDR_Event(event.getMessage()));
                });
      }

      tableView.setItems(FXCollections.observableArrayList(data));
      isReady = true;
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    //        .whenComplete(
    //            (page, exc) -> {
    //              currentPage = page;
    //              page.iterator()
    //              .forEachRemaining(
    //                  event -> {
    //                    data.add(new CMDR_Event(event.getMessage()));
    //                  });
    //              currentPage.getNextPage().get((nextPage, exec)->
    //              {
    //                page.iterator()
    //                .forEachRemaining(
    //                    event -> {
    //                      data.add(new CMDR_Event(event.getMessage()));
    //                    });
    //                currentPage = page;
    //              });
    //              tableView.setItems(FXCollections.observableArrayList(data));
    //              isReady = true;
    //              EventLogInstance.logger.log(Level.WARNING, "Events-->" + data.toString());
    //              //              Collections.reverse(eventList); // Output is reverse
    // chronological
    //            });
  }

  private Node createPage(int pageIndex) {
    EventLogInstance.logger.log(Level.WARNING, "createPage#1" + pageIndex);

    int fromIndex = pageIndex * rowsPerPage;
    int toIndex = Math.min(fromIndex + rowsPerPage, data.size());
    //    int toIndex = 16;
    EventLogInstance.logger.log(Level.WARNING, "createPage#2");

    EventLogInstance.logger.log(Level.WARNING, "Events-->" + data.toString());

    tableView.setItems(FXCollections.observableArrayList(data.subList(fromIndex, toIndex)));

    EventLogInstance.logger.log(Level.WARNING, "createPage#3");

    return new BorderPane(tableView);
  }
}
