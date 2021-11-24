package com.windhoverlabs.commander.applications.connections;

import org.phoebus.ui.javafx.ImageCache;

import com.windhoverlabs.commander.core.YamcsServerConnection;
import com.windhoverlabs.commander.core.YamcsServerContext;
import com.windhoverlabs.commander.core.OLD_CMDR_YamcsInstance;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
	private TreeView<YamcsModelItemCell> contextTree;

	@FXML
	public void initialize() {
		initCellValueFactories();

	}

	private void initCellValueFactories() {
		YamcsServerContext newContext = new YamcsServerContext();

		YamcsModelItemCell newRootCell = new YamcsModelItemCell(newContext, true, "");
		TreeItem<YamcsModelItemCell> connectionTreeItem = new TreeItem<YamcsModelItemCell>(newRootCell);

		if (contextTree.getRoot() == null) {
			contextTree.setRoot(connectionTreeItem);
		}
		contextTree.setCellFactory(cell -> new YamcsServerItemTreeCell());

	}

	Image addserverConnectionImmage = ImageCache.getImage(ConnectionsManagerApp.class,
			"/icons/add_server_connection.png");
	Image removeServerConnectionImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");
	Image activateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/activate.png");
	Image deactivateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");

	public void createContextMenu() {

		final ContextMenu contextMenu = new ContextMenu();
		MenuItem addServerConnection = new MenuItem("Add Server", new ImageView(addserverConnectionImmage));
		addServerConnection.setOnAction(e -> {
			NewConnectionDialog dialog = null;
			dialog = new NewConnectionDialog();
			YamcsServerConnection newServer = dialog.showAndWait().orElse(null);

			if (newServer == null)
				return;

			YamcsServerContext newChildContext = new YamcsServerContext(newServer);

			newChildContext.connect();

			YamcsServerContext newContext = new YamcsServerContext();

			YamcsModelItemCell newCell = new YamcsModelItemCell(newContext, false,
					newChildContext.getConnection().toString());

			TreeItem<YamcsModelItemCell> childYamcsContextTreeItem = new TreeItem<YamcsModelItemCell>(newCell);

			contextTree.getRoot().getChildren().add(childYamcsContextTreeItem);

			for (OLD_CMDR_YamcsInstance node : newChildContext.getNodes()) {
				YamcsModelItemCell newYamcsInstanceCell = new YamcsModelItemCell(newContext, false,
						node.getInstanceName());

				TreeItem<YamcsModelItemCell> newYamcsInstanceCellTreeItem = new TreeItem<YamcsModelItemCell>(
						newYamcsInstanceCell);

				contextTree.getRoot().getChildren().get(contextTree.getRoot().getChildren().size() - 1).getChildren()
						.add(newYamcsInstanceCellTreeItem);
			}

		});

		contextMenu.getItems().add(addServerConnection);

		if (isSelectionServerTreeItem()) {

			MenuItem activateServerConnection = new MenuItem("Activate Connection", new ImageView(activateImage));
			MenuItem deactivateServerConnection = new MenuItem("Deactivate Connection", new ImageView(deactivateImage));

			MenuItem removeServerConnection = new MenuItem("Remove Connection",
					new ImageView(removeServerConnectionImage));

			removeServerConnection.setOnAction(e -> {
				// TODO
				contextTree.selectionModelProperty().getValue().getSelectedItems().forEach(item -> {
					contextTree.getRoot().getChildren().remove(item);
				});
			});

			contextMenu.getItems().add(removeServerConnection);
			contextMenu.getItems().add(activateServerConnection);
			contextMenu.getItems().add(deactivateServerConnection);
		}

//		else if (isSelectionNodeTreeItem()) {
//			MenuItem activateNodeMenuItem = new MenuItem("Activate Node", new ImageView(activateImage));
//			MenuItem deactivateNodeMenuItem = new MenuItem("Deactivate Node", new ImageView(deactivateImage));
//
//			activateNodeMenuItem.setOnAction(e -> {
//				contextTree.selectionModelProperty().getValue().getSelectedItems().forEach(item -> {
//					((YamcsServerContext) item.getValue()).getNodes().get(item.getParent().getChildren().indexOf(item))
//							.activate();
//				});
//				contextTree.refresh();
//
//			});
//
//			deactivateNodeMenuItem.setOnAction(e -> {// generateNodes(5, newChildContext);
//
//				contextTree.selectionModelProperty().getValue().getSelectedItems().forEach(item -> {
//					((YamcsServerContext) item.getValue()).getNodes().get(item.getParent().getChildren().indexOf(item))
//							.deactivate();
//				});
//				contextTree.refresh();
//			});
//
//			contextMenu.getItems().add(activateNodeMenuItem);
//			contextMenu.getItems().add(deactivateNodeMenuItem);
//		}

		contextTree.setContextMenu(contextMenu);

	}

	private boolean isSelectionNodeTreeItem() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isSelectionServerTreeItem() {
		// TODO Auto-generated method stub
		return false;
	}

//	private void initCellValueFactories() {
//		contextTree.setCellFactory(param -> new TreeCell<NodeContext>() {
//            @Override
//            protected void updateItem(NodeContext node, boolean empty) {
//                super.updateItem(node, empty);
//                
////                System.out.println(param.);
//                if (node == null || empty) {
////                    setGraphic(null);
//                    System.out.println("if");
//                } else {
//                	System.out.println("else");
////                    EmployeeControl employeeControl = new EmployeeControl(employee);
////                    employeeControl.setOnMouseClicked(mouseEvent -> label.setText(employee.getName()));
////                    setGraphic(new Label("Node"));
//                }
//            }
//        });
//		
//	}
}
