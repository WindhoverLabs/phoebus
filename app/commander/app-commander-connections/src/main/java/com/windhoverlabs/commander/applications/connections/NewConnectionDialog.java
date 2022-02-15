package com.windhoverlabs.commander.applications.connections;

import com.windhoverlabs.commander.core.YamcsServerConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

public class NewConnectionDialog extends Dialog<YamcsServerConnection> {
  private final TextField serverUrl = new TextField();
  private final TextField port = new TextField();
  private final TextField user = new TextField();
  private final TextField serverName = new TextField();
  // TODO:Make a decision on policy.
  private final PasswordField password = new PasswordField();

  final GridPane layout = new GridPane();

  public NewConnectionDialog(Callback<YamcsServerConnection, Boolean> testConnectionCallback) {
    this.setTitle("New Connection");
    ButtonType testConnectionButtonType = new ButtonType("Test Connection", ButtonData.OTHER);
    ButtonType connectButtonType = new ButtonType("Connect", ButtonData.OK_DONE);

    layout.setColumnIndex(layout, null);
    layout.setHgap(5);
    layout.setVgap(5);
    setResizable(true);
    layout.setPrefWidth(600);

    addServerNameField();
    addServerUrlField();
    addPortField();
    addUserField();
    addPasswordField();

    getDialogPane().setContent(layout);
    getDialogPane()
        .getButtonTypes()
        .addAll(connectButtonType, ButtonType.CANCEL, testConnectionButtonType);

    Button testConnectionButton = (Button) getDialogPane().lookupButton(testConnectionButtonType);
    testConnectionButton.addEventFilter(
        ActionEvent.ACTION,
        event -> {
          YamcsServerConnection newConnection =
              new YamcsServerConnection(serverUrl.getText(), Integer.parseInt(port.getText()));
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

    Platform.runLater(() -> serverUrl.requestFocus());

    Button connectButton = (Button) getDialogPane().lookupButton(connectButtonType);

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
          if (button.getText() == "Connect") {
            try {
              newConnection =
                  new YamcsServerConnection(serverUrl.getText(), Integer.parseInt(port.getText()));

              newConnection.setName(serverName.getText());
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

  private void addServerNameField() {
    serverName.setPromptText("Alice");
    layout.add(new Label("Name:"), 0, 0);
    serverName.setTooltip(new Tooltip("Server name is used for pvs."));
    GridPane.setHgrow(serverName, Priority.ALWAYS);
    layout.add(serverName, 1, 0);
  }

  private void addServerUrlField() {
    serverUrl.setPromptText("168.2.5.100");
    layout.add(new Label("Server Url:"), 0, 1);
    serverUrl.setTooltip(new Tooltip("Name of the server url to connect to."));
    GridPane.setHgrow(serverUrl, Priority.ALWAYS);
    layout.add(serverUrl, 1, 1);
  }

  private void addPortField() {
    port.setPromptText("1234");
    layout.add(new Label("Port:"), 0, 2);
    port.setTooltip(new Tooltip("Port number to connect to."));
    GridPane.setHgrow(port, Priority.ALWAYS);
    layout.add(port, 1, 2);
  }

  private void addUserField() {
    layout.add(new Label("Username:"), 0, 4);
    user.setTooltip(new Tooltip("Username, if necessary."));
    GridPane.setHgrow(user, Priority.ALWAYS);
    layout.add(user, 1, 4);
  }

  // TODO PLEASE. Make a decision on policy before releasing this to users.
  private void addPasswordField() {
    layout.add(new Label("Password:"), 0, 5);
    password.setTooltip(new Tooltip("Password, if necessary."));
    GridPane.setHgrow(password, Priority.ALWAYS);
    layout.add(password, 1, 5);
  }
}
