package com.windhoverlabs.yamcs.applications.commandhistory;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.yamcs.client.Helpers;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;

public class CommandHistoryController {
  public static final Logger log =
      Logger.getLogger(CommandHistoryController.class.getPackageName());

  private final TableView<CommandHistoryEntry> tableView = new TableView<CommandHistoryEntry>();

  TableColumn<CommandHistoryEntry, String> timeCol =
      new TableColumn<CommandHistoryEntry, String>("Time");
  TableColumn<CommandHistoryEntry, String> commentCol =
      new TableColumn<CommandHistoryEntry, String>("Comment");
  TableColumn<CommandHistoryEntry, String> commandCol =
      new TableColumn<CommandHistoryEntry, String>("Command");
  TableColumn<CommandHistoryEntry, String> receptionTimeCol =
      new TableColumn<CommandHistoryEntry, String>("Reception Time");
  TableColumn<CommandHistoryEntry, String> messageCol =
      new TableColumn<CommandHistoryEntry, String>("Message");
  TableColumn<CommandHistoryEntry, String> typeCol =
      new TableColumn<CommandHistoryEntry, String>("Type");
  TableColumn<CommandHistoryEntry, String> sourceCol =
      new TableColumn<CommandHistoryEntry, String>("Source");
  TableColumn<CommandHistoryEntry, String> instanceCol =
      new TableColumn<CommandHistoryEntry, String>("Instance");

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
    tableView.setId("commahdHistoryTable");
    //    tableView.getStylesheets().add(CommandHistoryApp.getCSSPath());
    //    messageCol.setCellValueFactory(
    //        (command) -> {
    //          return new SimpleStringProperty(command.getValue().getMessage());
    //        });

    //    CSS Code might be useful for Q, R, S columns
    //    messageCol.setCellFactory(
    //        column -> {
    //          return new TableCell<CommandHistoryEntry, String>() {
    //            @Override
    //            protected void updateItem(String item, boolean empty) {
    //              super.updateItem(item, empty); // This is mandatory
    //              setTextFill(Color.BLACK);
    //              if (item == null || empty) { // If the cell is empty
    //                setText(null);
    //                setStyle("");
    //              } else { // If the cell is not empty
    //                // We get here all the info of the event of this row
    //                CommandHistoryEntry event = getTableView().getItems().get(getIndex());
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
    //                setText(item); // Put the String data in the cell
    //              }
    //            }
    //          };
    //        });

    commandCol.setCellValueFactory(
        (command) -> {
          return new SimpleStringProperty(command.getValue().getCommandName());
        });
    timeCol.setCellValueFactory(
        (command) -> {
          return new SimpleStringProperty(
              Helpers.toInstant(command.getValue().getGenerationTime()).toString());
        });
    //    TODO: Comment might be a command attr
    //    commentCol.setCellValueFactory(
    //            (command) -> {
    //              return new SimpleStringProperty(
    //                 command.getValue().ge() ) ;
    //            });
    //    typeCol.setCellValueFactory(
    //        (event) -> {
    //          return new SimpleStringProperty(event.getValue().getType().toString());
    //        });
    //    sourceCol.setCellValueFactory(
    //        (event) -> {
    //          return new SimpleStringProperty(event.getValue().getSource().toString());
    //        });
    //    instanceCol.setCellValueFactory(
    //        (event) -> {
    //          return new SimpleStringProperty(YamcsObjectManager.getDefaultInstance().getName());
    //        });
    tableView.getColumns().addAll(timeCol, commentCol, commandCol);

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
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getCommands());
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
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getCommands());
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
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getCommands());
              tableView.refresh();
            }
          }
        };

    YamcsObjectManager.addYamcsListener(yamcsListener);
    Callback<CommandHistoryEntry, Boolean> callback =
        new Callback<CommandHistoryEntry, Boolean>() {
          @Override
          public Boolean call(CommandHistoryEntry param) {
            // TODO Auto-generated method stub
            return true;
          }
        };
    //    createEventButton.setOnAction(
    //        e -> {
    //          var dialog = new NewEventDialog(callback, "");
    //          CommandHistoryEntry newEvent = dialog.showAndWait().orElse(null);
    //
    //          if (newEvent == null) {
    //            log.warning("Failed to send event.");
    //          }
    //
    //          YamcsServer s = YamcsObjectManager.getDefaultServer();
    //          if (s == null) {
    //            log.warning("Failed to find default server");
    //            return;
    //          }
    //          CMDR_YamcsInstance instance = YamcsObjectManager.getDefaultInstance();
    //          if (instance == null) {
    //            log.warning("Failed to find default instance");
    //            return;
    //          }
    //          instance.publishEvent(newEvent, s.getYamcsClient());
    //        });

    gridPane.add(tableView, 0, 1);
  }

  public CommandHistoryController() {}

  public void unInit() {
    YamcsObjectManager.removeListener(yamcsListener);
  }
}
