package com.windhoverlabs.yamcs.applications.commandoptions;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_YamcsInstance.CommandOption;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import org.phoebus.ui.javafx.EditCell;

public class CommandOptionsController {
  public static final Logger log =
      Logger.getLogger(CommandOptionsController.class.getPackageName());

  @FXML private TableView<CommandOption> tableView;

  private static final int dataSize = 10_023;

  // TODO:Eventually these will be in spinner nodes. These are the event filters.
  private String currentServer = "sitl";
  private String currentInstance = "yamcs-cfs";

  YamcsAware yamcsListener = null;

  @FXML private GridPane gridPane;

  @FXML private ToggleButton updateButton;

  @FXML private TableColumn<CommandOption, String> optionColumn;
  @FXML private TableColumn<CommandOption, String> valueColumn;

  public Node getRootPane() {
    return gridPane;
  }

  @FXML
  public void initialize() {
    tableView.setId("commandOptionsTable");
    tableView.getStylesheets().add(CommandOptionsApp.getCSSPath());

    valueColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getValue()));
    ;

    valueColumn.setEditable(true);

    // Eventually swap createStringEditCell with an instance of BoolComboEditCell
    valueColumn.setCellFactory(list -> EditCell.createStringEditCell());

    tableView.setEditable(true);

    valueColumn.setOnEditCommit(
        event -> {
          event.getRowValue().setValue(event.getNewValue());

          Platform.runLater(() -> System.out.println("Some GUI processing"));
        });

    optionColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));

    yamcsListener =
        new YamcsAware() {
          public void changeDefaultInstance() {
            if (YamcsObjectManager.getDefaultInstance() != null) {
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getOptionsList());
              tableView.refresh();
            }
          }

          public void onYamcsConnected() {
            if (YamcsObjectManager.getDefaultInstance() != null) {
              tableView.setItems(YamcsObjectManager.getDefaultInstance().getOptionsList());
            }
          }

          public void onInstancesReady(YamcsServer s) {
            if (s.getDefaultInstance() != null) {

              tableView.setItems(YamcsObjectManager.getDefaultInstance().getOptionsList());
            }
          }
        };

    YamcsObjectManager.addYamcsListener(yamcsListener);
  }

  public void unInit() {
    YamcsObjectManager.removeListener(yamcsListener);
  }
}
