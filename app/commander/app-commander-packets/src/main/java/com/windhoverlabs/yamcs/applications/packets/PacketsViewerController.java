package com.windhoverlabs.yamcs.applications.packets;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import com.windhoverlabs.yamcs.core.YamcsWebSocketClient.TmStatistics;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.css.SimpleStyleableStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;

public class PacketsViewerController {
  public static final Logger log = Logger.getLogger(PacketsViewerController.class.getPackageName());

  private final TableView<TmStatistics> tableView = new TableView<TmStatistics>();

  TableColumn<TmStatistics, String> packetRate =
      new TableColumn<TmStatistics, String>("Packet Rate");
  TableColumn<TmStatistics, String> packetNameCol = new TableColumn<TmStatistics, String>("Packet");
  TableColumn<TmStatistics, String> packetTimeCol =
      new TableColumn<TmStatistics, String>("Packet  Time");
  TableColumn<TmStatistics, String> rcvdTimeCol = new TableColumn<TmStatistics, String>("Received");

  TableColumn<TmStatistics, String> dataRateCol =
      new TableColumn<TmStatistics, String>("Data Rate");
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
    rcvdTimeCol.setCellValueFactory(
        (tm) -> {
          if (tm != null && tm.getValue() != null) {
            return new SimpleStringProperty((tm.getValue().lastReceived));
          } else {
            return new SimpleStringProperty("");
          }
        });

    rcvdTimeCol.setCellValueFactory(
        (tm) -> {
          if (tm != null && tm.getValue() != null) {
            return new SimpleStringProperty((tm.getValue().lastReceived));
          } else {
            return new SimpleStringProperty("");
          }
        });

    dataRateCol.setCellValueFactory(
        (tm) -> {
          if (tm != null && tm.getValue() != null) {
            return new SimpleStringProperty((tm.getValue().dataRate));
          } else {
            return new SimpleStringProperty("");
          }
        });

    packetTimeCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.2));
    packetNameCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));
    packetTimeCol.minWidthProperty().bind(tableView.widthProperty().multiply(0.2));
    packetNameCol.minWidthProperty().bind(tableView.widthProperty().multiply(0.3));

    tableView
        .getColumns()
        .addAll(packetNameCol, packetTimeCol, packetRate, rcvdTimeCol, dataRateCol);

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

    gridPane.add(tableView, 0, 0);
  }

  public PacketsViewerController() {}

  public void unInit() {
    YamcsObjectManager.removeListener(yamcsListener);
  }
}
