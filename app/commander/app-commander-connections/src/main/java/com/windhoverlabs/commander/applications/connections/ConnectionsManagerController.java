package com.windhoverlabs.commander.applications.connections;

import static com.windhoverlabs.commander.applications.connections.ConnectionsManagerInstance.logger;

import org.phoebus.ui.javafx.ImageCache;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
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
    TreeTableView<Object> serverConnectionsTableView;

	public ConnectionsManagerController() {
	}

	@FXML
	public void initialize() {
	}

	Image addProperties = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/server_connection.png");

	@FXML
	public void createContextMenu() {
		
		final ContextMenu contextMenu = new ContextMenu();
		// Add property to channel
		MenuItem addServerConnection = new MenuItem("Add Connection", new ImageView(addProperties));
		addServerConnection.setOnAction(e -> {

			// get the list of server connections?
//            properties = getClient().getAllProperties();
//            AddPropertyDialog dialog = new AddPropertyDialog(tableView, properties);
//            Optional<Property> result = dialog.showAndWait();
//            result.ifPresent(property -> {
//                if (addPropertyJob != null) {
//                    addPropertyJob.cancel();
//                }
//                List<String> channelNames = tableView.getSelectionModel().getSelectedItems().stream().map(ch -> {
//                    return ch.getName();
//                }).collect(Collectors.toList());
//                AddProperty2ChannelsJob.submit(getClient(),
//                        channelNames,
//                        property,
//                        (url, ex) -> ExceptionDetailsErrorDialog.openError("ChannelFinder Query Error", ex.getMessage(), ex));

//            });
//        });
		}

		);
		
		contextMenu.getItems().add(addServerConnection);
		
		serverConnectionsTableView.setContextMenu(contextMenu);

	}

	/** Call when no longer needed */
	public void shutdown() {
		System.out.println("shutdown");
	}
}
