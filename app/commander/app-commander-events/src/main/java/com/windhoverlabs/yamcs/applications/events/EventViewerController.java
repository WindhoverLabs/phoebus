package com.windhoverlabs.yamcs.applications.events;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_Event;
import com.windhoverlabs.yamcs.core.CMDR_YamcsInstance;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.yamcs.protobuf.Event.EventSeverity;

public class EventViewerController {
  public static final Logger log = Logger.getLogger(EventViewerController.class.getPackageName());

  private final TableView<CMDR_Event> tableView = new TableView<CMDR_Event>();

  TableColumn<CMDR_Event, String> severityCol = new TableColumn<CMDR_Event, String>("Severity");
  TableColumn<CMDR_Event, String> annotationCol = new TableColumn<CMDR_Event, String>();
  TableColumn<CMDR_Event, String> generationTimeCol =
      new TableColumn<CMDR_Event, String>("Generation Time");
  TableColumn<CMDR_Event, String> receptionTimeCol =
      new TableColumn<CMDR_Event, String>("Reception Time");
  TableColumn<CMDR_Event, String> messageCol = new TableColumn<CMDR_Event, String>("Message");
  TableColumn<CMDR_Event, String> typeCol = new TableColumn<CMDR_Event, String>("Type");
  TableColumn<CMDR_Event, String> sourceCol = new TableColumn<CMDR_Event, String>("Source");
  TableColumn<CMDR_Event, String> instanceCol = new TableColumn<CMDR_Event, String>("Instance");

  // TODO:Eventually these will be in spinner nodes. These are the event filters.
  private String currentServer = "sitl";
  private String currentInstance = "yamcs-cfs";

  YamcsAware yamcsListener = null;

  @FXML private GridPane gridPane;

  @FXML private ToggleButton scrollLockButton;
  @FXML private Button createEventButton;

  public Node getRootPane() {
    return gridPane;
  }

  @FXML
  public void initialize() {
    tableView.setId("eventsTable");
    tableView.getStylesheets().add(EventViewerApp.getCSSPath());
    messageCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getMessage());
        });

    generationTimeCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getGenerationTime().toString());
        });
    receptionTimeCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getReceptionTime().toString());
        });
    severityCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getSeverity().toString());
        });

    severityCol.setCellFactory(
        column -> {
          return new TableCell<CMDR_Event, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
              super.updateItem(item, empty); // This is mandatory
              setTextFill(Color.BLACK);
              if (item == null || empty) { // If the cell is empty
                setText(null);
                setStyle("");
              } else { // If the cell is not empty
                // We get here all the info of the event of this row

                this.getStyleClass().clear();
                switch (EventSeverity.valueOf(item)) {
                  case CRITICAL:
                    this.getStyleClass().add("critical");
                    break;
                  case DISTRESS:
                    this.getStyleClass().add("distress");
                    break;
                  case ERROR:
                    this.getStyleClass().add("error");
                    break;
                  case INFO:
                    this.getStyleClass().add("info");
                    break;
                  case SEVERE:
                    this.getStyleClass().add("severe");
                    break;
                  case WARNING:
                    this.getStyleClass().add("warning");
                    break;
                  case WATCH:
                    this.getStyleClass().add("watch");
                    break;
                  default:
                    setTextFill(Color.BLACK);
                    break;
                }
                setText(item); // Put the String data in the cell
              }
            }
          };
        });
    typeCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getType().toString());
        });
    sourceCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(event.getValue().getSource().toString());
        });
    instanceCol.setCellValueFactory(
        (event) -> {
          return new SimpleStringProperty(YamcsObjectManager.getDefaultInstance().getName());
        });
    tableView
        .getColumns()
        .addAll(
            messageCol,
            generationTimeCol,
            receptionTimeCol,
            severityCol,
            typeCol,
            sourceCol,
            instanceCol);

    yamcsListener =
        new YamcsAware() {
          public void changeDefaultInstance() {
            if (YamcsObjectManager.getDefaultInstance() != null) {
              YamcsObjectManager.getDefaultInstance()
                  .getEvents()
                  .addListener(
                      new ListChangeListener<Object>() {
                        @Override
                        public void onChanged(Change<?> c) {
                          Platform.runLater(
                              () -> {
                                if (!scrollLockButton.isSelected()) {
                                  tableView.scrollTo(tableView.getItems().size() - 1);
                                }
                              });
                        }
                      });
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getEvents());
              tableView.refresh();
            }
          }

          public void onYamcsConnected() {
            if (YamcsObjectManager.getDefaultInstance() != null) {
              YamcsObjectManager.getDefaultInstance()
                  .getEvents()
                  .addListener(
                      new ListChangeListener<Object>() {
                        @Override
                        public void onChanged(Change<?> c) {
                          Platform.runLater(
                              () -> {
                                if (!scrollLockButton.isSelected()) {
                                  tableView.scrollTo(tableView.getItems().size() - 1);
                                }
                              });
                        }
                      });
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getEvents());
              tableView.refresh();
            }
          }

          public void onInstancesReady(YamcsServer s) {
            if (s.getDefaultInstance() != null) {
              s.getDefaultInstance()
                  .getEvents()
                  .addListener(
                      new ListChangeListener<Object>() {
                        @Override
                        public void onChanged(Change<?> c) {
                          Platform.runLater(
                              () -> {
                                if (!scrollLockButton.isSelected()) {
                                  tableView.scrollTo(tableView.getItems().size() - 1);
                                }
                              });
                        }
                      });
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getEvents());
              tableView.refresh();
            }
          }
        };

    YamcsObjectManager.addYamcsListener(yamcsListener);
    Callback<CMDR_Event, Boolean> callback =
        new Callback<CMDR_Event, Boolean>() {
          @Override
          public Boolean call(CMDR_Event param) {
            // TODO Auto-generated method stub
            return true;
          }
        };
    createEventButton.setOnAction(
        e -> {
          var dialog = new NewEventDialog(callback, "");
          CMDR_Event newEvent = dialog.showAndWait().orElse(null);

          if (newEvent == null) {
            log.warning("Failed to send event.");
          }

          YamcsServer s = YamcsObjectManager.getDefaultServer();
          if (s == null) {
            log.warning("Failed to find default server");
            return;
          }
          CMDR_YamcsInstance instance = YamcsObjectManager.getDefaultInstance();
          if (instance == null) {
            log.warning("Failed to find default instance");
            return;
          }
          instance.publishEvent(newEvent, s.getYamcsClient());
        });

    gridPane.add(tableView, 0, 1);
  }

  public EventViewerController() {}

  private ObservableList<CMDR_Event> generateEvents(int numberOfEvents) {
    ObservableList<CMDR_Event> events = FXCollections.observableArrayList();
    for (int i = 0; i < numberOfEvents; i++) {
      DecimalFormat formatter = new DecimalFormat("#,###.00");
      events.add(
          new CMDR_Event(
              "Fake Events" + formatter.format(i + 1),
              Instant.now(),
              EventSeverity.INFO,
              "FAKE",
              Instant.now(),
              "FAKE_SOURCE",
              "FAKE_INSTANCE"));
    }
    return events;
  }

  public void unInit() {
    YamcsObjectManager.removeListener(yamcsListener);
  }
}
