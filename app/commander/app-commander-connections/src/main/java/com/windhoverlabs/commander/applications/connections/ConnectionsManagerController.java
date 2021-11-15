package com.windhoverlabs.commander.applications.connections;

import static com.windhoverlabs.commander.applications.connections.ConnectionsManagerInstance.logger;

import java.net.URL;
import java.util.ResourceBundle;

import org.phoebus.framework.nls.NLS;
import org.phoebus.ui.javafx.ImageCache;

import com.windhoverlabs.commander.core.YamcsConnection;
import com.windhoverlabs.commander.core.YamcsContext;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

/**
 * Controller for the Connections Manager App
 *
 * @lgomez
 */
@SuppressWarnings("nls")
public class ConnectionsManagerController {
	@FXML
	TreeTableView<YamcsContext> serverConnectionsTableView;
	@FXML
	TreeTableColumn<YamcsContext, String> serverColumn;
	@FXML
	TreeTableColumn<YamcsContext, String> userColumn;
	@FXML
	TreeTableColumn<YamcsContext, String> statusColumn;
	@FXML
	TreeTableColumn<YamcsContext, String> processorColumn;

	public ConnectionsManagerController() {
	}

	@FXML
	public void initialize() {
	}

	Image addserverConnectionImmage = ImageCache.getImage(ConnectionsManagerApp.class,
			"/icons/add_server_connection.png");
	Image removeServerConnectionImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");
	Image activateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/activate.png");
	Image deactivateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");

	YamcsContext yamcsContext = new YamcsContext("localhost", 8090, "John");

	public void createContextMenu() {

		YamcsContext newContext = new YamcsContext();
		TreeItem<YamcsContext> connectionTreeItem = new TreeItem<YamcsContext>(newContext);

		if (serverConnectionsTableView.getRoot() == null) {
			serverConnectionsTableView.setRoot(connectionTreeItem);
			serverConnectionsTableView.setShowRoot(false);
		}

		final ContextMenu contextMenu = new ContextMenu();
		// Add property to channelTreeItem
		MenuItem addServerConnection = new MenuItem("Add Connection", new ImageView(addserverConnectionImmage));
		addServerConnection.setOnAction(e -> {
			NewConnectionDialog dialog = null;
			dialog = new NewConnectionDialog();
			YamcsConnection newConnection = dialog.showAndWait().orElse(null);

			if (newConnection == null)
				return;

			YamcsContext newChildContext = new YamcsContext(newConnection);
			TreeItem<YamcsContext> childYamcsContextTreeItem = new TreeItem<YamcsContext>(newChildContext);

			initCellValueFactories();

			serverConnectionsTableView.getRoot().getChildren().add(childYamcsContextTreeItem);

		});

		MenuItem removeServerConnection = null;

		if (serverConnectionsTableView.selectionModelProperty().getValue().getSelectedItems().size() > 0) {

			removeServerConnection = new MenuItem("Remove Connection", new ImageView(removeServerConnectionImage));

			removeServerConnection.setOnAction(e -> {
				// TODO
				serverConnectionsTableView.selectionModelProperty().getValue().getSelectedItems().forEach(item -> {
					serverConnectionsTableView.getRoot().getChildren().remove(item);
				});
			});

		}

		MenuItem activateServerConnection = new MenuItem("Activate Connection", new ImageView(activateImage));
		MenuItem deactivateServerConnection = new MenuItem("Deactivate Connection", new ImageView(deactivateImage));

		contextMenu.getItems().add(addServerConnection);
		contextMenu.getItems().add(deactivateServerConnection);
		if (removeServerConnection != null) {
			contextMenu.getItems().add(removeServerConnection);
		}
		contextMenu.getItems().add(activateServerConnection);

		serverConnectionsTableView.setContextMenu(contextMenu);

	}

	private void initCellValueFactories() {
		serverColumn
				.setCellValueFactory(new Callback<CellDataFeatures<YamcsContext, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<YamcsContext, String> p) {
						return new ReadOnlyObjectWrapper<String>(p.getValue().getValue().getConnection().toString());
					}

				});

		userColumn.setCellValueFactory(new Callback<CellDataFeatures<YamcsContext, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<YamcsContext, String> p) {
//				p.getValue().getChildren().get(0).
				return new ReadOnlyObjectWrapper<String>(p.getValue().getValue().getConnection().getUser().toString());
			}

		});

		statusColumn
				.setCellValueFactory(new Callback<CellDataFeatures<YamcsContext, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<YamcsContext, String> p) {
						// p.getValue() returns the TreeItem<Person> instance for a particular
						// TreeTableView row,
						// p.getValue().getValue() returns the Person instance inside the
						// TreeItem<Person>
						p.getValue().getChildren().add(null);
						return new ReadOnlyObjectWrapper<String>(
								p.getValue().getValue().getConnection().getStatus().toString());
					}

				});
	}

	/** Call when no longer needed */
	public void shutdown() {
		
		System.out.println("shutdown");
	}
}
