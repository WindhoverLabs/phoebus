package com.windhoverlabs.yamcs.applications.packets;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_Event;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import com.windhoverlabs.yamcs.core.YamcsWebSocketClient.TmStatistics;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.SimpleStyleableStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import org.yamcs.protobuf.Event.EventSeverity;

public class PacketsViewerController {
  public static final Logger log = Logger.getLogger(PacketsViewerController.class.getPackageName());

  private final TableView<TmStatistics> tableView = new TableView<TmStatistics>();

  TableColumn<TmStatistics, String> packetRate =
      new TableColumn<TmStatistics, String>("Packet Rate");
  TableColumn<CMDR_Event, String> annotationCol = new TableColumn<CMDR_Event, String>();
  TableColumn<CMDR_Event, String> generationTimeCol =
      new TableColumn<CMDR_Event, String>("Generation Time");
  TableColumn<CMDR_Event, String> receptionTimeCol =
      new TableColumn<CMDR_Event, String>("Reception Time");
  TableColumn<TmStatistics, String> packetNameCol = new TableColumn<TmStatistics, String>("Packet");
  TableColumn<TmStatistics, String> packetTimeCol =
      new TableColumn<TmStatistics, String>("Packet  Time");

  YamcsAware yamcsListener = null;

  @FXML private GridPane gridPane;

  @FXML private ToggleButton scrollLockButton;

  public Node getRootPane() {
    return gridPane;
  }

  @FXML
  public void initialize() {
    tableView.setId("PacketsTable");
    packetNameCol.setCellValueFactory(
        (tm) -> {
          SimpleStyleableStringProperty s = new javafx.css.SimpleStyleableStringProperty(null);
          if (tm != null && tm.getValue() != null) {
            s.set(tm.getValue().packetName);
          }
          return s;
        });
    packetRate.setCellValueFactory(
        (tm) -> {
          if (tm != null && tm.getValue() != null) {
            return new SimpleStringProperty((tm.getValue().packetRate));
          } else {
            return new SimpleStringProperty("");
          }
        });

    packetTimeCol.setCellValueFactory(
        (tm) -> {
          if (tm != null && tm.getValue() != null) {
            return new SimpleStringProperty((tm.getValue().lastPacketTime));
          } else {
            return new SimpleStringProperty("");
          }
        });

    tableView.getColumns().addAll(packetNameCol, packetTimeCol, packetRate);

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
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getPackets());
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
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getPackets());
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
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getPackets());
              tableView.refresh();
            }
          }
        };

    YamcsObjectManager.addYamcsListener(yamcsListener);

    gridPane.add(tableView, 0, 1);
  }

  public PacketsViewerController() {
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
