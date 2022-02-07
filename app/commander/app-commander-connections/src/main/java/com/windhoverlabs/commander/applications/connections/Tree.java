package com.windhoverlabs.commander.applications.connections;

import static java.util.stream.Collectors.toList;

import com.windhoverlabs.commander.core.CMDR_YamcsInstance;
import com.windhoverlabs.commander.core.YamcsObject;
import com.windhoverlabs.commander.core.YamcsObjectManager;
import com.windhoverlabs.commander.core.YamcsServer;
import com.windhoverlabs.commander.core.YamcsServerConnection;
import java.util.Arrays;
import java.util.List;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class Tree {

  private final TreeView<YamcsObject<?>> treeView;

  private final List<Class<? extends YamcsObject<?>>> itemTypes =
      Arrays.asList(YamcsServer.class, CMDR_YamcsInstance.class);

  // TODO: Move this root handling to another model class. This would make it easier to decouple
  private YamcsObject<?> root;

  public Tree(ObservableList<YamcsServer> servers) {
    treeView = new TreeView<>();

    root = YamcsObjectManager.getRoot();

    TreeItem<YamcsObject<?>> treeRoot = createItem(root);

    treeView.setRoot(treeRoot);
    treeView.setShowRoot(false);

    treeView.setCellFactory(
        tv -> {
          TreeCell<YamcsObject<?>> cell =
              new TreeCell<YamcsObject<?>>() {

                @Override
                protected void updateItem(YamcsObject<?> item, boolean empty) {
                  super.updateItem(item, empty);
                  textProperty().unbind();
                  this.setStyle("");
                  if (empty) {
                    setText(null);
                    itemTypes.stream()
                        .map(Tree.this::asPseudoClass)
                        .forEach(pc -> pseudoClassStateChanged(pc, false));
                  } else {
                    textProperty().bind(item.nameProperty());
                    // Indicate default instances
                    if (item.getObjectType() == CMDR_YamcsInstance.OBJECT_TYPE) {
                      String serverName = this.getTreeItem().getParent().getValue().getName();
                      String instanceName = item.getName();
                      if (YamcsObjectManager.getServerFromName(serverName).getDefaultInstance()
                              != null
                          && YamcsObjectManager.getServerFromName(serverName)
                              .getDefaultInstance()
                              .getName()
                              .equals(instanceName)) {
                        this.setStyle("-fx-font-weight: bold");
                      } else {
                        this.setStyle("");
                      }
                    }
                    PseudoClass itemPC = asPseudoClass(item.getClass());
                    itemTypes.stream()
                        .map(Tree.this::asPseudoClass)
                        .forEach(pc -> pseudoClassStateChanged(pc, itemPC.equals(pc)));
                  }
                }
              };
          return cell;
        });

    // create a menu
    ContextMenu contextMenu = new ContextMenu();

    // create menuitems
    MenuItem addServer = new MenuItem("Add Server");
    addServer.setOnAction(
        e -> {
          NewConnectionDialog dialog = new NewConnectionDialog();
          YamcsServerConnection newServer = dialog.showAndWait().orElse(null);
          if (newServer == null) return;

          root.createAndAddChild(newServer.getName());

          ((YamcsServer) root.getItems().get(root.getItems().size() - 1)).connect(newServer);
        });

    MenuItem removeServer = new MenuItem("Remove Server");
    removeServer.setOnAction(
        e -> {
          TreeItem<YamcsObject<?>> selectedItem = treeView.getSelectionModel().getSelectedItem();

          if (selectedItem == null) {
            /* Nothing is selected. This is not supposed to happen. */
          } else {
            YamcsObject<?> selectedObject = selectedItem.getValue();

            if (selectedObject.getObjectType() != YamcsServer.OBJECT_TYPE) {
              /* Server is not selected. This is not supposed to happen. */
            } else {
              root.getItems().remove(selectedObject);
            }
          }
        });

    SeparatorMenuItem sep = new SeparatorMenuItem();
    MenuItem connectAll = new MenuItem("Connect All");
    MenuItem disconnectAll = new MenuItem("Disconnect All");
    MenuItem connectInstance = new MenuItem("Connect");
    MenuItem disconnectInstance = new MenuItem("Disconnect");
    MenuItem setAsDefault = new MenuItem("Set As Default");
    setAsDefault.setOnAction(
        e -> {
          TreeItem<YamcsObject<?>> selectedItem = treeView.getSelectionModel().getSelectedItem();

          if (selectedItem == null) {
            /* Nothing is selected. This is not supposed to happen. */
          } else {
            YamcsObject<?> selectedObject = selectedItem.getValue();

            if (selectedObject.getObjectType() == CMDR_YamcsInstance.OBJECT_TYPE) {
              String serverName = selectedItem.getParent().getValue().getName();
              String instanceName = selectedItem.getValue().getName();
              YamcsObjectManager.setDefaultInstance(serverName, instanceName);
              treeView.refresh();
            }
          }
        });

    // add menu items to menu
    contextMenu.getItems().add(addServer);
    contextMenu.getItems().add(removeServer);
    contextMenu.getItems().add(sep);
    contextMenu.getItems().add(connectAll);
    contextMenu.getItems().add(disconnectAll);
    contextMenu.getItems().add(connectInstance);
    contextMenu.getItems().add(disconnectInstance);
    contextMenu.getItems().add(setAsDefault);

    // setContextMenu to label
    treeView.setContextMenu(contextMenu);

    treeView.setOnContextMenuRequested(
        e -> {
          TreeItem<YamcsObject<?>> selectedItem = treeView.getSelectionModel().getSelectedItem();

          if (selectedItem == null) {
            /* Nothing is selected. */
            addServer.setDisable(false);
            removeServer.setDisable(true);
            connectAll.setDisable(false);
            disconnectAll.setDisable(false);
            connectInstance.setDisable(true);
            disconnectInstance.setDisable(true);
            setAsDefault.setDisable(true);
          } else {
            YamcsObject<?> selectedObject = selectedItem.getValue();

            if (selectedObject.getObjectType() == "root") {
              /* This is the root node. This shouldn't be possible. */
              addServer.setDisable(true);
              removeServer.setDisable(true);
              connectAll.setDisable(true);
              disconnectAll.setDisable(true);
              connectInstance.setDisable(true);
              disconnectInstance.setDisable(true);
              setAsDefault.setDisable(true);
            } else if (selectedObject.getObjectType() == YamcsServer.OBJECT_TYPE) {
              /* This is a server node. */
              addServer.setDisable(false);
              removeServer.setDisable(false);
              connectAll.setDisable(false);
              disconnectAll.setDisable(false);
              connectInstance.setDisable(false);
              disconnectInstance.setDisable(false);
              setAsDefault.setDisable(true);
            } else if (selectedObject.getObjectType() == CMDR_YamcsInstance.OBJECT_TYPE) {
              /* This is a instance node. */
              addServer.setDisable(true);
              removeServer.setDisable(true);
              connectAll.setDisable(false);
              disconnectAll.setDisable(false);
              connectInstance.setDisable(false);
              disconnectInstance.setDisable(false);
              setAsDefault.setDisable(false);
            } else {
              /* I don't know what this is. */
              addServer.setDisable(false);
              removeServer.setDisable(false);
              connectAll.setDisable(false);
              disconnectAll.setDisable(false);
              connectInstance.setDisable(false);
              disconnectInstance.setDisable(false);
              setAsDefault.setDisable(false);
            }
          }
        });
  }

  public TreeView<YamcsObject<?>> getTreeView() {
    return treeView;
  }

  private TreeItem<YamcsObject<?>> createItem(YamcsObject<?> object) {

    // create tree item with children from game object's list:

    TreeItem<YamcsObject<?>> item = new TreeItem<>(object);
    item.setExpanded(true);
    item.getChildren().addAll(object.getItems().stream().map(this::createItem).collect(toList()));

    // update tree item's children list if game object's list changes:

    object
        .getItems()
        .addListener(
            (Change<? extends YamcsObject<?>> c) -> {
              while (c.next()) {
                if (c.wasAdded()) {
                  item.getChildren()
                      .addAll(c.getAddedSubList().stream().map(this::createItem).collect(toList()));
                }
                if (c.wasRemoved()) {
                  item.getChildren()
                      .removeIf(treeItem -> c.getRemoved().contains(treeItem.getValue()));
                }
              }
            });

    return item;
  }

  private PseudoClass asPseudoClass(Class<?> clz) {
    return PseudoClass.getPseudoClass(clz.getSimpleName().toLowerCase());
  }

  @SuppressWarnings("unchecked")
  public YamcsObject<YamcsServer> getRoot() {
    return (YamcsObject<YamcsServer>) root;
  }
}