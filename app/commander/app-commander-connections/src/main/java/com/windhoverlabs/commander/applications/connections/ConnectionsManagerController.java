package com.windhoverlabs.commander.applications.connections;

import static com.windhoverlabs.commander.applications.connections.ConnectionsManagerInstance.logger;

import java.util.ArrayList;

import org.phoebus.ui.javafx.ImageCache;

import com.windhoverlabs.commander.core.YamcsConnection;
import com.windhoverlabs.commander.core.YamcsContext;
import com.windhoverlabs.commander.core.YamcsNode;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeItem.TreeModificationEvent;
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
		serverConnectionsTableView.setPlaceholder(new Label("Right-click to add connections."));
		initCellValueFactories();//			generateNodes(5, newChildContext);
		// TODO: Might help fix the issue when sorting an empty table.
//		serverConnectionsTableView.setOnSort(event -> {
//			if (serverConnectionsTableView.getSelectionModel().getSelectedIndices().size() > 1)
//				serverConnectionsTableView.getSelectionModel().clearSelection();
//		});

	}

	Image addserverConnectionImmage = ImageCache.getImage(ConnectionsManagerApp.class,
			"/icons/add_server_connection.png");
	Image removeServerConnectionImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");
	Image activateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/activate.png");
	Image deactivateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");

	public void createContextMenu() {
		System.out.println("1");
		YamcsContext newContext = new YamcsContext();
		TreeItem<YamcsContext> connectionTreeItem = new TreeItem<YamcsContext>(newContext);
//		System.out.println("2");

		if (serverConnectionsTableView.getRoot() == null) {
			serverConnectionsTableView.setRoot(connectionTreeItem);
			serverConnectionsTableView.setShowRoot(false);
		}
		
//		System.out.println("3");
		final ContextMenu contextMenu = new ContextMenu();
		// Add property to channelTreeItem
		MenuItem addServerConnection = new MenuItem("Add Connection", new ImageView(addserverConnectionImmage));
//		System.out.println("4");
		addServerConnection.setOnAction(e -> {
			NewConnectionDialog dialog = null;
			dialog = new NewConnectionDialog();
			YamcsConnection newConnection = dialog.showAndWait().orElse(null);

			if (newConnection == null)
				return;

			YamcsContext newChildContext = new YamcsContext(newConnection);
			
			newChildContext.connect();

			TreeItem<YamcsContext> childYamcsContextTreeItem = new TreeItem<YamcsContext>(newChildContext);

			System.out.println("Add Server Connection");

			serverConnectionsTableView.getRoot().getChildren().add(childYamcsContextTreeItem);

			for (YamcsNode node : newChildContext.getNodes()) {
				serverConnectionsTableView.getRoot().getChildren()
						.get(serverConnectionsTableView.getRoot().getChildren().size() - 1).getChildren()
						.add(new TreeItem<YamcsContext>(newChildContext));
			}

		});

		contextMenu.getItems().add(addServerConnection);

		if (isSelectionServerTreeItem()) {

			MenuItem activateServerConnection = new MenuItem("Activate Connection", new ImageView(activateImage));
			MenuItem deactivateServerConnection = new MenuItem("Deactivate Connection", new ImageView(deactivateImage));

			MenuItem removeServerConnection = new MenuItem("Remove Connection", new ImageView(removeServerConnectionImage));

			removeServerConnection.setOnAction(e -> {
				// TODO
				serverConnectionsTableView.selectionModelProperty().getValue().getSelectedItems().forEach(item -> {
					serverConnectionsTableView.getRoot().getChildren().remove(item);
				});
			});

			contextMenu.getItems().add(removeServerConnection);
			contextMenu.getItems().add(activateServerConnection);
			contextMenu.getItems().add(deactivateServerConnection);
		}

		else if (isSelectionNodeTreeItem()) {
			MenuItem activateNodeMenuItem = new MenuItem("Activate Node", new ImageView(activateImage));
			MenuItem deactivateNodeMenuItem = new MenuItem("Deactivate Node", new ImageView(deactivateImage));
			
			
			activateNodeMenuItem.setOnAction(e -> {
				serverConnectionsTableView.selectionModelProperty().getValue().getSelectedItems().forEach(item -> {
					item.getValue().getNodes().get(item.getParent().getChildren().indexOf(item)).activate();
				});
				serverConnectionsTableView.refresh();

			});

			deactivateNodeMenuItem.setOnAction(e -> {//			generateNodes(5, newChildContext);
				
				serverConnectionsTableView.selectionModelProperty().getValue().getSelectedItems().forEach(item -> {
					item.getValue().getNodes().get(item.getParent().getChildren().indexOf(item)).deactivate();
				});
				serverConnectionsTableView.refresh();
			});

			contextMenu.getItems().add(activateNodeMenuItem);
			contextMenu.getItems().add(deactivateNodeMenuItem);
		}

		serverConnectionsTableView.setContextMenu(contextMenu);

	}

	private boolean isSelectionServerTreeItem() {
		return serverConnectionsTableView.selectionModelProperty().getValue().getSelectedItems().size() > 0
				&& serverConnectionsTableView.getRoot().getChildren().size() > 0
				&& !serverConnectionsTableView.selectionModelProperty().getValue().getSelectedItems().get(0).isLeaf();
	}

	private boolean isSelectionNodeTreeItem() {
		return serverConnectionsTableView.selectionModelProperty().getValue().getSelectedItems().size() > 0
				&& serverConnectionsTableView.getRoot().getChildren().size() > 0
				&& serverConnectionsTableView.selectionModelProperty().getValue().getSelectedItems().get(0).isLeaf();
	}

	private void initCellValueFactories() {
		serverColumn
				.setCellValueFactory(new Callback<CellDataFeatures<YamcsContext, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<YamcsContext, String> p) {
						
						if (p.getValue().isLeaf() && p.getValue().getChildren().size()==0) {
							System.out.println("Leaf branch");
							return new ReadOnlyObjectWrapper<String>(p.getValue().getValue().getNodes()
									.get(p.getValue().getParent().getChildren().indexOf(p.getValue()))
									.getInstanceName());
						} else {
							return new ReadOnlyObjectWrapper<String>(
									p.getValue().getValue().getConnection().toString());
						}
					}

				});

		userColumn.setCellValueFactory(new Callback<CellDataFeatures<YamcsContext, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<YamcsContext, String> p) {
				if (p.getValue().isLeaf()) {
					return new ReadOnlyObjectWrapper<String>("");
				} else {
					return new ReadOnlyObjectWrapper<String>(
							p.getValue().getValue().getConnection().getUser().toString());
				}
			}

		});

		statusColumn
				.setCellValueFactory(new Callback<CellDataFeatures<YamcsContext, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<YamcsContext, String> p) {
						// TODO Fetch nodes from server and add them as children
						if (p.getValue().isLeaf()) {
							return new ReadOnlyObjectWrapper<String>(p.getValue().getValue().getNodes()
									.get(p.getValue().getParent().getChildren().indexOf(p.getValue())).getState()
									.toString());
						} else {
							return new ReadOnlyObjectWrapper<String>(
									p.getValue().getValue().getConnection().getStatus().toString());
						}
					}

				});

		processorColumn
				.setCellValueFactory(new Callback<CellDataFeatures<YamcsContext, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<YamcsContext, String> p) {
						// TODO Fetch nodes from server and add them as children
						if (p.getValue().isLeaf()) {
							return new ReadOnlyObjectWrapper<String>(p.getValue().getValue().getNodes()
									.get(p.getValue().getParent().getChildren().indexOf(p.getValue())).getProcessor());
						} else {
							return new ReadOnlyObjectWrapper<String>("");
						}
					}

				});
	}

	/** Call when no longer needed */
	public void shutdown() {
		System.out.println("shutdown");
	}
}
