package com.windhoverlabs.yamcs.applications.links;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_Event;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
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
import javafx.css.SimpleStyleableStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.yamcs.protobuf.Event.EventSeverity;

public class LinksViewerController {
  public static final Logger log = Logger.getLogger(LinksViewerController.class.getPackageName());

  private final TableView<org.yamcs.protobuf.links.LinkInfo> tableView =
      new TableView<org.yamcs.protobuf.links.LinkInfo>();

  TableColumn<org.yamcs.protobuf.links.LinkInfo, String> inCount =
      new TableColumn<org.yamcs.protobuf.links.LinkInfo, String>("In");
  TableColumn<CMDR_Event, String> annotationCol = new TableColumn<CMDR_Event, String>();
  TableColumn<CMDR_Event, String> generationTimeCol =
      new TableColumn<CMDR_Event, String>("Generation Time");
  TableColumn<CMDR_Event, String> receptionTimeCol =
      new TableColumn<CMDR_Event, String>("Reception Time");
  TableColumn<org.yamcs.protobuf.links.LinkInfo, String> nameCol =
      new TableColumn<org.yamcs.protobuf.links.LinkInfo, String>("Name");
  TableColumn<CMDR_Event, String> typeCol = new TableColumn<CMDR_Event, String>("Type");
  TableColumn<CMDR_Event, String> sourceCol = new TableColumn<CMDR_Event, String>("Source");
  TableColumn<CMDR_Event, String> instanceCol = new TableColumn<CMDR_Event, String>("Instance");

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  private ObservableList<CMDR_Event> data =
      FXCollections.observableArrayList(new ArrayList<CMDR_Event>());
  private static final int dataSize = 10_023;

  // TODO:Eventually these will be in spinner nodes. These are the event filters.
  private String currentServer = "sitl";
  private String currentInstance = "yamcs-cfs";

  YamcsAware yamcsListener = null;

  @FXML private GridPane gridPane;

  @FXML private ToggleButton scrollLockButton;

  public Node getRootPane() {
    return gridPane;
  }

  @FXML
  public void initialize() {
    tableView.setId("LinksTable");

    //    scheduler.scheduleAtFixedRate(
    //        () -> {
    //          //          this.outOfSync = this.logEventCount != this.streamEventCount;
    ////          tableView.refresh();
    //        },
    //        30,
    //        30,
    //        TimeUnit.SECONDS);
    //    tableView.getStylesheets().add(LinksViewerApp.getCSSPath());
    nameCol.setCellValueFactory(
        (link) -> {
          SimpleStyleableStringProperty s = new javafx.css.SimpleStyleableStringProperty(null);
          if (link != null && link.getValue() != null) {
            s.set(link.getValue().getName());
          }
          return s;
        });

    nameCol.setCellFactory(
        column -> {
          return new TableCell<org.yamcs.protobuf.links.LinkInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
              super.updateItem(item, empty); // This is mandatory
              setTextFill(Color.BLACK);
              if (item == null || empty) { // If the cell is empty
                setText(null);
                setStyle("");
              } else { // If the cell is not empty
                // We get here all the info of the event of this row
                //                CMDR_Event event = getTableView().getItems().get(getIndex());
                //                switch (event.getSeverity()) {
                //                  case CRITICAL:
                //                    this.getStyleClass().add("critical");
                //                    break;
                //                  case DISTRESS:
                //                    this.getStyleClass().add("distress");
                //                    break;
                //                  case ERROR:
                //                    this.getStyleClass().add("error");
                //                    break;
                //                  case INFO:
                //                    this.getStyleClass().add("info");
                //                    break;
                //                  case SEVERE:
                //                    this.getStyleClass().add("severe");
                //                    break;
                //                  case WARNING:
                //                    this.getStyleClass().add("warning");
                //                    break;
                //                  case WATCH:
                //                    this.getStyleClass().add("watch");
                //                    break;
                //                  default:
                //                    setTextFill(Color.BLACK);
                //                    break;
                //                }
                setText(item); // Put the String data in the cell
                Circle circle = new Circle();
                circle.setCenterX(100.0f);
                circle.setCenterY(100.0f);
                circle.setRadius(10.0f);
                var activeColor = Color.RED;
                if (YamcsObjectManager.getDefaultInstance() != null
                    && YamcsObjectManager.getDefaultInstance().getLinksMap().get(item) != null) {
                  if (YamcsObjectManager.getDefaultInstance().isLinkActive(item)) {
                    activeColor = Color.LIGHTGREEN;
                  } else {
                    //                    activeColor = activeColor.darker();
                    activeColor = Color.BLUE;
                  }
                }

                circle.setFill(activeColor);
                this.setGraphic(circle);
              }
            }
          };
        });
    inCount.setCellValueFactory(
        (link) -> {
          if (link != null && link.getValue() != null) {
            return new SimpleStringProperty(Long.toString(link.getValue().getDataInCount()));
          } else {
            return new SimpleStringProperty("");
          }
        });
    //    tableView
    //        .getColumns()
    //        .addAll(
    //            nameCol,
    //            generationTimeCol,
    //            receptionTimeCol,
    //            severityCol,
    //            typeCol,
    //            sourceCol,
    //            instanceCol);

    tableView.getColumns().addAll(nameCol, inCount);

    yamcsListener =
        new YamcsAware() {
          public void changeDefaultInstance() {
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
            tableView.setItems(YamcsObjectManager.getDefaultInstance().getLinks());
            tableView.refresh();
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
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getLinks());
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
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getLinks());
              tableView.refresh();
            }
          }

          //          public void updateLink(String link) {
          //            tableView.refresh();
          //          }
        };

    YamcsObjectManager.addYamcsListener(yamcsListener);

    System.out.println("items part of root-->" + YamcsObjectManager.getRoot().getItems());
    //    for (YamcsServer s : YamcsObjectManager.getRoot().getItems()) {
    //      s.addListener(yamcsListener);
    //    }

    gridPane.add(tableView, 0, 1);
  }

  public LinksViewerController() {
    System.out.println("EventViewerController constructor$$$$$$$$$$$$$$");
  }

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
