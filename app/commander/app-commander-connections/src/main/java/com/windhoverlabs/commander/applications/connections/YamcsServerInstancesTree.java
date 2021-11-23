package com.windhoverlabs.commander.applications.connections;

import java.util.ArrayList;
import java.util.HashMap;

import org.phoebus.ui.javafx.ImageCache;

import com.windhoverlabs.commander.core.CMDR_YamcsInstance;
import com.windhoverlabs.commander.core.YamcsServer;
import com.windhoverlabs.commander.core.YamcsServerContext;

import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * A tree model for YAMCS servers loosely based on
 * org.phoebus.applications.pvtree.ui.FXTree
 * 
 * @author lgomez
 */

public class YamcsServerInstancesTree {

	private final TreeView<YamcsModelItemCell> treeView = new TreeView<YamcsModelItemCell>();

	ArrayList<YamcsServerContext> server = new ArrayList<YamcsServerContext>();
	YamcsServerContext newContext = new YamcsServerContext();

	YamcsModelItemCell rootCell = new YamcsModelItemCell(null, true, "");
	TreeItem<YamcsModelItemCell> rootTreeItem = new TreeItem<YamcsModelItemCell>(rootCell);

	HashMap<String, CMDR_YamcsInstance> pathToYamcsInstance = new HashMap<String, CMDR_YamcsInstance>();

	Image addserverConnectionImmage = ImageCache.getImage(ConnectionsManagerApp.class,
			"/icons/add_server_connection.png");
	Image removeServerConnectionImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");
	Image activateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/activate.png");
	Image deactivateImage = ImageCache.getImage(ConnectionsManagerApp.class, "/icons/delete.png");

	public YamcsServerInstancesTree() {
		treeView.setOnContextMenuRequested(e -> {
			createContextMenu();
		});
		addRoot();
	}

	private void addRoot() {

		if (treeView.getRoot() == null) {
			treeView.setRoot(rootTreeItem);
		}
		treeView.setCellFactory(cell -> new YamcsServerItemTreeCell());
	}

	public void addServer(YamcsServer newServer) {
		YamcsServerContext newContext = new YamcsServerContext(newServer);
		YamcsModelItemCell newCell = new YamcsModelItemCell(newContext, false, newContext.getConnection().toString());

		TreeItem<YamcsModelItemCell> childYamcsContextTreeItem = new TreeItem<YamcsModelItemCell>(newCell);
		treeView.getRoot().getChildren().add(childYamcsContextTreeItem);

		newContext.connect();

		addInstances(newContext);
	}

	private void addInstances(YamcsServerContext newContext) {
		for (CMDR_YamcsInstance node : newContext.getNodes()) {
			YamcsModelItemCell newYamcsInstanceCell = new YamcsModelItemCell(newContext, false, node.getInstanceName());

			TreeItem<YamcsModelItemCell> newYamcsInstanceCellTreeItem = new TreeItem<YamcsModelItemCell>(
					newYamcsInstanceCell);
			
			treeView.getRoot().getChildren().get(treeView.getRoot().getChildren().size() - 1).getChildren()
					.add(newYamcsInstanceCellTreeItem);
			
			pathToYamcsInstance.put(getInstanceTreeItemPath(newYamcsInstanceCellTreeItem), node);
		}
	}

	public TreeView<YamcsModelItemCell> getTreeView() {
		return treeView;
	}

	public String getInstanceTreeItemPath(TreeItem<YamcsModelItemCell> instanceTreeItem) {
		if (instanceTreeItem.getValue().isRoot()) {
			return null;
		}

		else if (instanceTreeItem.getParent().getValue().isRoot()) {
			// This is a server node, and since we are looking for instance paths, this path
			// is considered invalid.
			return null;
		}

		// The item itself nor the parent itself is root, so this must be an instance
		// node
		else {
			return instanceTreeItem.getParent().getValue().getData() + ":" + instanceTreeItem.getValue().getData();
		}
	}

	public CMDR_YamcsInstance getInstance(String instancePath) {
		return pathToYamcsInstance.get(instancePath);
	}
	
	public CMDR_YamcsInstance getInstance(TreeItem<YamcsModelItemCell> instanceTreeModelItem) {
		return pathToYamcsInstance.get(getInstanceTreeItemPath(instanceTreeModelItem));
	}

	public void createContextMenu() {

		final ContextMenu contextMenu = new ContextMenu();
		MenuItem addServerConnection = new MenuItem("Add Server", new ImageView(addserverConnectionImmage));
		addServerConnection.setOnAction(e -> {
			NewConnectionDialog dialog = null;
			dialog = new NewConnectionDialog();
			YamcsServer newServer = dialog.showAndWait().orElse(null);

			if (newServer == null)
				return;

			addServer(newServer);

		});
		
		MenuItem queryInstance = new MenuItem("Query Instance");
		queryInstance.setOnAction(e -> {
//			getInstance(treeView.get);
		});

		contextMenu.getItems().addAll(addServerConnection, queryInstance);

		if (isSelectionServerTreeItem()) {

			MenuItem activateServerConnection = new MenuItem("Activate Connection", new ImageView(activateImage));
			MenuItem deactivateServerConnection = new MenuItem("Deactivate Connection", new ImageView(deactivateImage));

			MenuItem removeServerConnection = new MenuItem("Remove Connection",
					new ImageView(removeServerConnectionImage));

			removeServerConnection.setOnAction(e -> {
				// TODO
				treeView.selectionModelProperty().getValue().getSelectedItems().forEach(item -> {
					treeView.getRoot().getChildren().remove(item);
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

		treeView.setContextMenu(contextMenu);

	}

	private boolean isSelectionServerTreeItem() {
		// TODO Auto-generated method stub
		return true;
	}

}
