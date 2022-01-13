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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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

  //  final ScheduledFuture<?> beeperHandle =
  //      scheduler.scheduleAtFixedRate(
  //          () -> {
  //            streamEvents();
  //          },
  //          10,
  //          10,
  //          TimeUnit.SECONDS);

  //    scheduler.schedule(new Runnable() {
  //      public void run() { beeperHandle.cancel(true); }
  //    }, 60 * 60, TimeUnit.SECONDS);
  public static final Logger log = Logger.getLogger(EventViewerController.class.getPackageName());

  private static final int MAX_PAGES = 100;

  //  public class CMDR_Event {
  //    public SimpleStringProperty message;
  //
  //    public CMDR_Event(String newMessage) {
  //      message = new SimpleStringProperty(newMessage);
  //    }
  //
  //    public String toString() {
  //      return message.getValue();
  //    }
  //  }

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

  private Page<Event> currentPage;
  //  @FXML private Pagination pagination;

  @FXML private GridPane gridPane;

  @FXML private ToggleButton streamButton;

  private boolean isReady = false;

  public Node getRootPane() {
    return gridPane;
  }

  @FXML
  public void initialize() {

    //    subscription = YamcsPlugin.getYamcsClient().createEventSubscription();
    //    subscription.addMessageListener(event -> {
    //        Display.getDefault().asyncExec(() -> processEvent(event));
    //    });
    //    scheduler.schedule(
    //        () -> {
    //          streamEvents();
    //        },
    //        10,
    //        TimeUnit.SECONDS);
    tableView.setId("eventsTable");
    //    tableView.
    //    streamButton.setOnAction(
    //        e -> {
    //          if (streamButton.isSelected()) {
    //            YamcsObjectManager.getDefaultInstance()
    //                .getEventSubscription()
    //                .addMessageListener(
    //                    new MessageListener<Event>() {
    //                      @Override
    //                      public void onMessage(Event message) {
    //                        log.warning("Streaming events...");
    //                        data.add(new CMDR_Event(message.getMessage()));
    //                        log.warning("data size-->" + data.size());
    //                      }
    //                    });
    //            if (YamcsObjectManager.getDefaultInstance() != null) {
    //              tableView.setItems(YamcsObjectManager.getDefaultInstance().getEvents());
    //              //              streamEvents();
    //            }
    //          }
    //        });
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
    //    updateData();
    tableView.setItems(YamcsObjectManager.getDefaultInstance().getEvents());
    //    tableView.setItems(generateEvents(1000000));
    gridPane.add(tableView, 0, 1);
    root = YamcsObjectManager.getRoot();
    //    pagination.setPageFactory(this::createPage);
    //    pagination.setPageCount(MAX_PAGES);
  }

  //  private void streamEvents() {
  //    System.out.println("streamEvents");
  //    if (YamcsObjectManager.getDefaultInstance() != null) {
  //      data.clear();
  //      YamcsObjectManager.getDefaultInstance()
  //          .getYamcsArchiveClient()
  //          .streamEvents(
  //              (message) -> {
  //                log.warning("Streaming events...");
  //                data.add(new CMDR_Event(message.getMessage()));
  //                log.warning("data size-->" + data.size());
  //                Platform.runLater(
  //                    () -> {
  //                      tableView.refresh();
  //                      System.out.println("tableView--> size" + tableView.getItems().size());
  //                    });
  //              },
  //              null,
  //              null);
  //    }
  //  }

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
