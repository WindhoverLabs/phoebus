package com.windhoverlabs.commander.applications.eventlog;

import com.windhoverlabs.commander.core.CMDR_Event;
import com.windhoverlabs.commander.core.YamcsObject;
import com.windhoverlabs.commander.core.YamcsObjectManager;
import com.windhoverlabs.commander.core.YamcsServer;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.yamcs.client.Page;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;

public class EventViewerController {

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public static final Logger log = Logger.getLogger(EventViewerController.class.getPackageName());

  private static final int MAX_PAGES = 100;

  private final TableView<CMDR_Event> tableView = new TableView<CMDR_Event>();

  TableColumn<CMDR_Event, String> severityCol = new TableColumn<CMDR_Event, String>();
  TableColumn<CMDR_Event, String> annotationCol = new TableColumn<CMDR_Event, String>();

  TableColumn<CMDR_Event, String> generationTimeCol =
      new TableColumn<CMDR_Event, String>("Generation Time");

  TableColumn<CMDR_Event, String> messageCol = new TableColumn<CMDR_Event, String>("Message");

  private ObservableList<CMDR_Event> data =
      FXCollections.observableArrayList(new ArrayList<CMDR_Event>());
  private static final int dataSize = 10_023;

  private int rowsPerPage = 100;

  // TODO:Eventually these will be in spinner nodes. These are the event filters.
  private String currentServer = "sitl";
  private String currentInstance = "yamcs-cfs";

  private YamcsObject<YamcsServer> root;
  private boolean scrollLock = true;

  private Page<Event> currentPage;
  //  @FXML private Pagination pagination;

  @FXML private GridPane gridPane;

  @FXML private ToggleButton scrollLockButton;

  private boolean isReady = false;

  public Node getRootPane() {
    return gridPane;
  }

  @FXML
  public void initialize() {
    tableView.setId("eventsTable");
    messageCol.setCellValueFactory(
        (event) -> {
          return event.getValue().getMessage();
        });

    messageCol.setCellFactory(
        column -> {
          return new TableCell<CMDR_Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
              super.updateItem(item, empty); // This is mandatory

              if (item == null || empty) { // If the cell is empty
                setText(null);
                setStyle("");
              } else { // If the cell is not empty
                // We get here all the info of the event of this row
                CMDR_Event event = getTableView().getItems().get(getIndex());
                switch (event.getSeverity()) {
                  case CRITICAL:
                    setTextFill(Color.RED);
                    break;
                  case DISTRESS:
                    setTextFill(Color.DARKRED);
                    break;
                  case ERROR:
                    setTextFill(Color.ORANGE);
                    break;
                  case INFO:
                    break;
                  case SEVERE:
                    //                    setTextFill(Color.);
                    break;
                  case WARNING:
                    setTextFill(Color.ORANGERED);
                    break;
                  case WATCH:
                    break;
                  default:
                    break;
                }
                setText(item); // Put the String data in the cell
              }
            }
          };
        });

    generationTimeCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getGenerationTime().toString());
        });
    tableView.getColumns().addAll(messageCol, generationTimeCol);
    YamcsObjectManager.getDefaultInstance()
        .getEvents()
        .addListener(
            new ListChangeListener<Object>() {
              @Override
              public void onChanged(Change<?> c) {
                System.out.println("items changed");
                Platform.runLater(
                    () -> {
                      if (scrollLockButton.isSelected()) {
                        tableView.scrollTo(tableView.getItems().size() - 1);
                      }
                    });
              }
            });
    tableView.setItems(YamcsObjectManager.getDefaultInstance().getEvents());
    gridPane.add(tableView, 0, 1);
    root = YamcsObjectManager.getRoot();
  }

  public EventViewerController() {}

  private Node createPage(int pageIndex) {
    int fromIndex = pageIndex * rowsPerPage;
    int toIndex = Math.min(fromIndex + rowsPerPage, data.size());

    //    tableView.setItems(data.subList(fromIndex, toIndex));
    tableView.setItems(data);
    return tableView;
  }

  private ObservableList<CMDR_Event> generateEvents(int numberOfEvents) {
    ObservableList<CMDR_Event> events = FXCollections.observableArrayList();
    for (int i = 0; i < numberOfEvents; i++) {
      DecimalFormat formatter = new DecimalFormat("#,###.00");
      events.add(
          new CMDR_Event(
              "Fake Events" + formatter.format(i + 1), Instant.now(), EventSeverity.INFO));
    }
    return events;
  }
}
