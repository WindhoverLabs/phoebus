package com.windhoverlabs.commander.applications.connections;

import com.windhoverlabs.yamcs.core.YamcsServerConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.util.Callback;

public class EditConnectionDialog extends NewConnectionDialog {
  private YamcsServerConnection connectionToEdit = null;

  public EditConnectionDialog(
      Callback<YamcsServerConnection, Boolean> testConnectionCallback,
      YamcsServerConnection newConnectionToEdit,
      String newCSSPath) {
    super(testConnectionCallback, newCSSPath);
    this.setTitle("Edit Connection");
    connectionToEdit = newConnectionToEdit;
    serverName.setText(connectionToEdit.getName());
    serverUrl.setText(connectionToEdit.getUrl());
    port.setText(Integer.toString(connectionToEdit.getPort()));
  }

  protected void addButtons(Callback<YamcsServerConnection, Boolean> testConnectionCallback) {

    ButtonType testConnectionButtonType = new ButtonType("Test Connection", ButtonData.OTHER);
    ButtonType saveButtonType = new ButtonType("Save Changes", ButtonData.OK_DONE);
    getDialogPane()
        .getButtonTypes()
        .addAll(saveButtonType, ButtonType.CANCEL, testConnectionButtonType);

    Button testConnectionButton = (Button) getDialogPane().lookupButton(testConnectionButtonType);
    testConnectionButton.addEventFilter(
        ActionEvent.ACTION,
        event -> {
          YamcsServerConnection newConnection =
              new YamcsServerConnection("", serverUrl.getText(), Integer.parseInt(port.getText()));
          Alert dialog = new Alert(AlertType.INFORMATION);
          if (testConnectionCallback.call(newConnection)) {
            dialog.setContentText("Connection is OK.");
          } else {
            dialog.setAlertType(AlertType.ERROR);
            dialog.setContentText("Connection test failed.");
          }

          dialog.showAndWait();
          event.consume();
        });

    Platform.runLater(() -> serverName.requestFocus());

    Button connectButton = (Button) getDialogPane().lookupButton(saveButtonType);

    connectButton.addEventFilter(
        ActionEvent.ACTION,
        event -> {
          if (!validateConnect()) {
            event.consume();
          }
        });

    setResultConverter(
        button -> {
          YamcsServerConnection newConnection = null;
          if (button.getText().equals("Save Changes")) {
            try {
              newConnection =
                  new YamcsServerConnection(
                      serverName.getText(), serverUrl.getText(), Integer.parseInt(port.getText()));

            } catch (Exception e) {
              Logger // Initial focus on name
                  .getLogger(getClass().getName())
                  .log(Level.WARNING, "Cannot format string to integer", e);
            }
          }

          return newConnection;
        });
  }

  private boolean validateConnect() {
    boolean isValid = true;
    if (serverUrl.getText() == null
        || serverUrl.getText().trim().isEmpty()
        || serverName.getText().trim().isEmpty()
        || port.getText().trim().isEmpty()) {
      isValid = false;
    }

    if (serverName.getText().trim().isEmpty()) {
      String Style = serverName.getStyle();

      if (!Style.contains("error")) {
        serverName.setStyle("error");
      }
    }
    return isValid;
  }
}
