package com.windhoverlabs.commander.applications.connections;

import static com.windhoverlabs.commander.applications.connections.ConnectionsManagerInstance.logger;

import java.net.URL;
import java.util.ResourceBundle;

import org.phoebus.framework.nls.NLS;
import org.phoebus.ui.javafx.ImageCache;

import com.windhoverlabs.commander.core.YamcsConnection;
import com.windhoverlabs.commander.core.YamcsContext;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
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
	@FXML
	TreeTableColumn<YamcsContext, String> serverColumn;
	@FXML
	TreeTableColumn<YamcsContext, String> userColumn;
	@FXML
	TreeTableColumn<YamcsContext, String> processorColumn;

	YamcsContext yamcsContext = new YamcsContext("localhost", 8090, "John");

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

	@FXML
	public void createContextMenu() {

		TreeItem<YamcsContext> connection = new TreeItem<YamcsContext>(new YamcsContext("localhost", 8090, "John"));

//		TreeItem audi1 = new TreeItem(new Car("Audi", "A1"));
//
//		audi.getChildren().add(audi1);
//		audi.getChildren().add(audi2);
//		audi.getChildren().add(audi3);

//		cars.getChildren().add(audi);
//		cars.getChildren().add(mercedes);

		serverConnectionsTableView.setRoot(connection);

//		serverConnectionsTableView.getTreeItem(0).setValue(new YamcsContext("localhost", 8090, "John"));

//		serverColumn.setCellValueFactory(new Callback<CellDataFeatures<YamcsContext, String>, ObservableValue<String>>() {
//					public ObservableValue<String> call(CellDataFeatures<YamcsContext, String> p) {
//						// p.getValue() returns the TreeItem<Person> instance for a particular
//						// TreeTableView row,
//						// p.getValue().getValue() returns the Person instance inside the
//						// TreeItem<Person>
//						return new ReadOnlyObjectWrapper<String>(p.getValue().getValue().getUrl());
//					}
//
//				});
//
//		userColumn.setCellValueFactory(new Callback<CellDataFeatures<YamcsContext, String>, ObservableValue<String>>() {
//			public ObservableValue<String> call(CellDataFeatures<YamcsContext, String> p) {
//				// p.getValue() returns the TreeItem<Person> instance for a particular
//				// TreeTableView row,
//				// p.getValue().getValue() returns the Person instance inside the
//				// TreeItem<Person>
//				return new ReadOnlyObjectWrapper<String>(p.getValue().getValue().getUser());
//			}
//
//		});

		final ContextMenu contextMenu = new ContextMenu();
		// Add property to channel
		MenuItem addServerConnection = new MenuItem("Add Connection", new ImageView(addserverConnectionImmage));
		addServerConnection.setOnAction(e -> {
			NewConnectionDialog dialog = null;
			dialog = new NewConnectionDialog();
			YamcsConnection newConnection = dialog.showAndWait().orElse(null);

			if (newConnection == null)
				return;

			System.out.println("new connection:" + newConnection.toString());
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
