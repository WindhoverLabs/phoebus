package com.windhoverlabs.commander.applications.connections;

import java.util.ArrayList;

import org.phoebus.ui.javafx.ImageCache;

import com.windhoverlabs.commander.core.NodeContext;
import com.windhoverlabs.commander.core.YamcsConnection;
import com.windhoverlabs.commander.core.YamcsContext;
import com.windhoverlabs.commander.core.YamcsNode;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
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
	private TreeView<NodeContext> contextTree;

	private NodeContext rootContext;

	@FXML
	public void initialize() {
		initCellValueFactories();
//		contextTree.getRoot().getChildren()
//		serverConnectionsTreeView.setPlaceholder(new Label("Right-click to add connections."));
//		initCellValueFactories();

	}

	private void initCellValueFactories() {
		YamcsContext newContext = new YamcsContext();

//		switch (newContext.getType()) {
//			case YAMCS: {
//				 newContext = new YamcsContext();
//
//			}
//		}

		TreeItem<YamcsContext> connectionTreeItem = new TreeItem<YamcsContext>(newContext);

		if (contextTree.getRoot() == null) {
//			contextTree.setRoot(connectionTreeItem);
		}
		contextTree.setCellFactory(cell -> new ContextCellModel<YamcsContext>("YAMCS", true));

	}

	Image addserverConnectionImmage = ImageCache.getImage(ConnectionsManagerApp.class,
			"/icons/add_server_connection.png");
	Image removeServerConnectionImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");
	Image activateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/activate.png");
	Image deactivateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");

	public void createContextMenu() {
		System.out.println("1");
		YamcsContext newContext = new YamcsContext();
		TreeItem<NodeContext> connectionTreeItem = new TreeItem<NodeContext>(newContext);

		if (contextTree.getRoot() == null) {
			contextTree.setRoot(connectionTreeItem);
		}

		final ContextMenu contextMenu = new ContextMenu();
		MenuItem addServerConnection = new MenuItem("Add Connection", new ImageView(addserverConnectionImmage));
		addServerConnection.setOnAction(e -> {
			NewConnectionDialog dialog = null;
			dialog = new NewConnectionDialog();
			YamcsConnection newConnection = dialog.showAndWait().orElse(null);

			if (newConnection == null)
				return;

			YamcsContext newChildContext = new YamcsContext(newConnection);

			newChildContext.connect();

			ArrayList<YamcsNode> newNodes = new ArrayList<YamcsNode>();

			newNodes.add(new YamcsNode("newNode"));

			newChildContext.setNodes(newNodes);

			TreeItem<NodeContext> childYamcsContextTreeItem = new TreeItem<NodeContext>(newChildContext);

			System.out.println("Add Server Connection");

			contextTree.getRoot().getChildren().add(childYamcsContextTreeItem);

			for (YamcsNode node : newChildContext.getNodes()) {
				contextTree.getRoot().getChildren().get(contextTree.getRoot().getChildren().size() - 1).getChildren()
						.add(new TreeItem<NodeContext>(newChildContext));
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

		else if (isSelectionNodeTreeItem()) {
			MenuItem activateNodeMenuItem = new MenuItem("Activate Node", new ImageView(activateImage));
			MenuItem deactivateNodeMenuItem = new MenuItem("Deactivate Node", new ImageView(deactivateImage));

			activateNodeMenuItem.setOnAction(e -> {
				contextTree.selectionModelProperty().getValue().getSelectedItems().forEach(item -> {
					((YamcsContext) item.getValue()).getNodes().get(item.getParent().getChildren().indexOf(item))
							.activate();
				});
				contextTree.refresh();

			});

			deactivateNodeMenuItem.setOnAction(e -> {// generateNodes(5, newChildContext);

				contextTree.selectionModelProperty().getValue().getSelectedItems().forEach(item -> {
					((YamcsContext) item.getValue()).getNodes().get(item.getParent().getChildren().indexOf(item))
							.deactivate();
				});
				contextTree.refresh();
			});

			contextMenu.getItems().add(activateNodeMenuItem);
			contextMenu.getItems().add(deactivateNodeMenuItem);
		}

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
