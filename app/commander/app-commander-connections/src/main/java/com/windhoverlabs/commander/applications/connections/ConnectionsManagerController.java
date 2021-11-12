package com.windhoverlabs.commander.applications.connections;

import static com.windhoverlabs.commander.applications.connections.ConnectionsManagerInstance.logger;
import java.util.logging.Level;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.phoebus.framework.nls.NLS;
import org.phoebus.ui.javafx.ImageCache;

import com.windhoverlabs.commander.core.YamcsContext;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller for the Connections Manager App
 *
 * @lgomez
 */
@SuppressWarnings("nls")
public class ConnectionsManagerController {
    @FXML
    TreeTableView<YamcsContext> serverConnectionsTableView;
    
	public ConnectionsManagerController() {
	}

	@FXML
	public void initialize() {
	}

	Image addserverConnectionImmage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/add_server_connection.png");
	Image removeServerConnectionImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");
	Image activateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/activate.png");
	Image deactivateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");

	@FXML
	public void createContextMenu() {
		
		final ContextMenu contextMenu = new ContextMenu();
		// Add property to channel
		MenuItem addServerConnection = new MenuItem("Add Connection", new ImageView(addserverConnectionImmage));
		addServerConnection.setOnAction(e -> {
			System.out.println("Clicked on addServerConnection");
//			final FXMLLoader fxmlLoader;
//			Dialog<String> dialog = null;
//			final URL fxml = getClass().getResource("NewConnection.fxml");
//			final ResourceBundle bundle = NLS.getMessages(ConnectionsManagerInstance.class);
//			fxmlLoader = new FXMLLoader(fxml, bundle);
//		     try {
//		    	 dialog = fxmlLoader.load();
//			     dialog.showAndWait();
//			} catch (IOException e1) {
//				logger.log( Level.WARNING, e1.toString());
//			}
		}

		);
		
		MenuItem removeServerConnection = new MenuItem("Remove Connection", new ImageView(removeServerConnectionImage));
		
		
		MenuItem activateServerConnection = new MenuItem("Activate Connection", new ImageView(activateImage));
		
		
		MenuItem deactivateServerConnection = new MenuItem("Deactivate Connection", new ImageView(deactivateImage));
		
		contextMenu.getItems().add(addServerConnection);
		contextMenu.getItems().add(removeServerConnection);
		contextMenu.getItems().add(deactivateServerConnection);
		contextMenu.getItems().add(activateServerConnection);

		serverConnectionsTableView.setContextMenu(contextMenu);

	}

	/** Call when no longer needed */
	public void shutdown() {
		System.out.println("shutdown");
	}
}
