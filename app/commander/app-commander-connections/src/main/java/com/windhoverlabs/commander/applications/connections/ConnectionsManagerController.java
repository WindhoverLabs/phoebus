package com.windhoverlabs.commander.applications.connections;

import com.windhoverlabs.commander.core.YamcsContext;

import javafx.fxml.FXML;
import javafx.scene.control.TreeView;

/**
 * Controller for the Connections Manager App
 *
 * @lgomez
 */
@SuppressWarnings("nls")
public class ConnectionsManagerController {
		
	@FXML
	TreeView<YamcsContext> contextTree;

	public ConnectionsManagerController() {
	}

	@FXML
	public void initialize() {
	}

	public void createContextMenu() {
	}

	/** Call when no longer needed */
	public void shutdown() {
		System.out.println("shutdown");
	}
}
