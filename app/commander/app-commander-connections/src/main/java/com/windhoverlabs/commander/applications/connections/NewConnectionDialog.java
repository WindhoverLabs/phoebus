package com.windhoverlabs.commander.applications.connections;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.phoebus.ui.autocomplete.PVAutocompleteMenu;
import com.windhoverlabs.commander.core.YamcsServer;
import com.windhoverlabs.commander.core.YamcsServer.YamcsConnectionStatus;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class NewConnectionDialog extends Dialog<YamcsServer> {
	private final TextField serverUrl = new TextField();
	private final TextField port = new TextField();
	private final TextField user = new TextField();
	// TODO:Make a decision on policy.
	private final PasswordField password = new PasswordField();

	final GridPane layout = new GridPane();

	public NewConnectionDialog() {

		ButtonType testConnectionButtonType = new ButtonType("Test Connection", ButtonData.OTHER);
		
		ButtonType connectButtonType = new ButtonType("Connect", ButtonData.OK_DONE);

		layout.setColumnIndex(layout, null);
		// layout.setGridLinesVisible(true);
		layout.setHgap(5);
		layout.setVgap(5);

		addServerUrlField();
		addPortField();
		addUserField();
		addPasswordField();

		getDialogPane().setContent(layout);
		getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL, testConnectionButtonType);

		Button testConnectionButton = (Button) getDialogPane().lookupButton(testConnectionButtonType);

		testConnectionButton.addEventFilter(ActionEvent.ACTION, event -> {
			// Test Connection Logic?
			
			System.out.println("Clicked on button");
			// to prevent the dialog from closing
			event.consume();
		});

		setResizable(true);

		this.getResultConverter();


		layout.setPrefWidth(600);

		Platform.runLater(() -> serverUrl.requestFocus());

		setResultConverter(button -> {
			YamcsServer newConnection = null;
			if (button.getButtonData() == ButtonData.OK_DONE) {
				try {
					newConnection = new YamcsServer(serverUrl.getText(), Integer.parseInt(port.getText()),
							user.getText());
					newConnection.setStatus(YamcsConnectionStatus.Connected);
				} catch (NumberFormatException e) {
					Logger // Initial focus on name
							.getLogger(getClass().getName()).log(Level.WARNING, "Cannot format string to integer", e);
				}
			}

			return newConnection;
		});
	}

	private void addServerUrlField() {
		layout.add(new Label("Server Url:"), 0, 0);
		serverUrl.setTooltip(new Tooltip("Name of the server url to connect to."));
		GridPane.setHgrow(serverUrl, Priority.ALWAYS);
		layout.add(serverUrl, 1, 0);
	}

	private void addPortField() {
		layout.add(new Label("Port:"), 0, 1);
		port.setTooltip(new Tooltip("Port number to connect to."));
		GridPane.setHgrow(port, Priority.ALWAYS);
		layout.add(port, 1, 1);
	}

	private void addUserField() {
		layout.add(new Label("Username:"), 0, 2);
		user.setTooltip(new Tooltip("Username, if necessary."));
		GridPane.setHgrow(user, Priority.ALWAYS);
		layout.add(user, 1, 2);
	}

	// TODO PLEASE. Make a decision on policy before releasing this to users.
	private void addPasswordField() {
		layout.add(new Label("password:"), 0, 3);
		password.setTooltip(new Tooltip("Password, if necessary."));
		GridPane.setHgrow(password, Priority.ALWAYS);
		layout.add(password, 1, 3);
	}
}
