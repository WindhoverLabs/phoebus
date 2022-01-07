package com.windhoverlabs.commander.applications.eventlog;

import com.windhoverlabs.commander.applications.eventlog.EventLogController.CMDR_Event;
import com.windhoverlabs.commander.core.YamcsObject;
import com.windhoverlabs.commander.core.YamcsObjectManager;
import com.windhoverlabs.commander.core.YamcsServer;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import org.yamcs.client.Page;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogController {

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

  private final TableView<EventLogController.CMDR_Event> tableView =
      new TableView<EventLogController.CMDR_Event>();

  TableColumn<EventLogController.CMDR_Event, String> severityCol =
      new TableColumn<EventLogController.CMDR_Event, String>();
  TableColumn<EventLogController.CMDR_Event, String> annotationCol =
      new TableColumn<EventLogController.CMDR_Event, String>();

  TableColumn<EventLogController.CMDR_Event, String> timeStampCol =
      new TableColumn<EventLogController.CMDR_Event, String>();

  TableColumn<EventLogController.CMDR_Event, String> messageCol =
      new TableColumn<EventLogController.CMDR_Event, String>("Message");

  private ArrayList<EventLogController.CMDR_Event> data =
      new ArrayList<EventLogController.CMDR_Event>();
  private static final int dataSize = 10_023;

  private int rowsPerPage = 100;

  // TODO:Eventually these will be in spinner nodes. These are the event filters.
  private String currentServer = "sitl";
  private String currentInstance = "yamcs-cfs";

  private YamcsObject<YamcsServer> root;

  private Page<Event> currentPage;
  @FXML private Pagination pagination;

  @FXML private Node gridPane;

  private boolean isReady = false;

  public Node getRootPane() {
    return gridPane;
  }

  @FXML
  public void initialize() {
    tableView.setId("eventsTable");
    messageCol.setCellValueFactory(
        (event) -> {
          return event.getValue().message;
        });

    messageCol.setCellFactory(
        column -> {
          return new TableCell<EventLogController.CMDR_Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
              super.updateItem(item, empty); // This is mandatory

              if (item == null || empty) { // If the cell is empty
                setText(null);
                setStyle("");
              } else { // If the cell is not empty
                setTextFill(Color.RED);
                setText(item); // Put the String data in the cell
              }
            }
          };
        });
    tableView.getColumns().add(messageCol);
    updateData();

    root = YamcsObjectManager.getRoot();
    pagination.setPageFactory(this::createPage);
    pagination.setPageCount(MAX_PAGES);
  }

  public EventLogController() {}

  @FXML
  private void updateData() {
    data.clear();
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
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private Node createPage(int pageIndex) {
    int fromIndex = pageIndex * rowsPerPage;
    int toIndex = Math.min(fromIndex + rowsPerPage, data.size());

    tableView.setItems(FXCollections.observableArrayList(data.subList(fromIndex, toIndex)));
    return tableView;
  }
}
