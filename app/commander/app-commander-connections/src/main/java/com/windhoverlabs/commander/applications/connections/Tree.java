package com.windhoverlabs.commander.applications.connections;

import static java.util.stream.Collectors.toList;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.yamcs.core.CMDR_YamcsInstance;
import com.windhoverlabs.yamcs.core.ConnectionState;
import com.windhoverlabs.yamcs.core.YamcsObject;
import com.windhoverlabs.yamcs.core.YamcsObjectManager;
import com.windhoverlabs.yamcs.core.YamcsServer;
import com.windhoverlabs.yamcs.core.YamcsServerConnection;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javafx.collections.ListChangeListener.Change;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class Tree {

  @FXML private TreeView<YamcsObject<?>> treeView;

  Logger logger = Logger.getLogger(Tree.class.getPackageName());

  private final List<Class<? extends YamcsObject<?>>> itemTypes =
      Arrays.asList(YamcsServer.class, CMDR_YamcsInstance.class);

  // TODO: Move this root handling to another model class. This would make it easier to decouple
  private YamcsObject<?> root;

  @FXML
  public void initialize() {
    root = YamcsObjectManager.getRoot();

    TreeItem<YamcsObject<?>> treeRoot = createItem(root);

    treeView.setRoot(treeRoot);
    treeView.setShowRoot(false);

    Tooltip t = new Tooltip("Right-click to add a new connection");

    treeView.setTooltip(t);

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
                    } else if (item.getObjectType() == YamcsServer.OBJECT_TYPE) {
                      textProperty().bind(((YamcsServer) item).getServerStateStrProperty());
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
    MenuItem addServer = new MenuItem("Add Connection");
    addServer.setOnAction(
        e -> {
          Callback<YamcsServerConnection, Boolean> callback =
              new Callback<YamcsServerConnection, Boolean>() {

                @Override
                public Boolean call(YamcsServerConnection connection) {
                  return YamcsServer.testConnection(connection);
                }
              };
          NewConnectionDialog dialog = new NewConnectionDialog(callback, "");
          YamcsServerConnection newServer = dialog.showAndWait().orElse(null);
          if (newServer == null) return;

          root.createAndAddChild(newServer.getName());

          YamcsServer lastAddedChild =
              (YamcsServer) root.getItems().get(root.getItems().size() - 1);
          lastAddedChild.setConnection(newServer);
          attemptToConnect(lastAddedChild);
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
              ((YamcsServer) selectedObject).disconnect();
              root.getItems().remove(selectedObject);
            }
          }
        });

    SeparatorMenuItem sep = new SeparatorMenuItem();
    MenuItem connectAllServers = new MenuItem("Connect All Servers");

    connectAllServers.setOnAction(
        e -> {
          TreeItem<YamcsObject<?>> selectedItem = treeView.getSelectionModel().getSelectedItem();

          if (selectedItem == null) {
            /* Nothing is selected. This is not supposed to happen. */
          } else {
            YamcsObject<?> selectedObject = selectedItem.getValue();

            if (selectedObject.getObjectType() != YamcsServer.OBJECT_TYPE) {
              /* Server is not selected. This is not supposed to happen. */
            } else {
              for (YamcsServer s : YamcsObjectManager.getRoot().getItems()) {
                attemptToConnect(s);
              }
            }
          }
        });

    MenuItem disconnectAllServers = new MenuItem("Disconnect All Servers");
    disconnectAllServers.setOnAction(
        e -> {
          TreeItem<YamcsObject<?>> selectedItem = treeView.getSelectionModel().getSelectedItem();

          if (selectedItem == null) {
            /* Nothing is selected. This is not supposed to happen. */
          } else {
            YamcsObject<?> selectedObject = selectedItem.getValue();

            if (selectedObject.getObjectType() != YamcsServer.OBJECT_TYPE) {
              /* Server is not selected. This is not supposed to happen. */
            } else {
              ((YamcsServer) selectedObject).disconnect();
            }
          }
        });
    MenuItem connectServer = new MenuItem("Connect");
    connectServer.setOnAction(
        e -> {
          TreeItem<YamcsObject<?>> selectedItem = treeView.getSelectionModel().getSelectedItem();

          if (selectedItem == null) {
            /* Nothing is selected. This is not supposed to happen. */
          } else {
            YamcsObject<?> selectedObject = selectedItem.getValue();

            if (selectedObject.getObjectType() != YamcsServer.OBJECT_TYPE) {
              /* Server is not selected. This is not supposed to happen. */
            } else {

              attemptToConnect(((YamcsServer) selectedObject));
            }
          }
        });
    MenuItem disconnectServer = new MenuItem("Disconnect");
    disconnectServer.setOnAction(
        e -> {
          TreeItem<YamcsObject<?>> selectedItem = treeView.getSelectionModel().getSelectedItem();
          if (selectedItem == null) {
            /* Nothing is selected. This is not supposed to happen. */
          } else {
            YamcsObject<?> selectedObject = selectedItem.getValue();

            if (selectedObject.getObjectType() != YamcsServer.OBJECT_TYPE) {
              /* Server is not selected. This is not supposed to happen. */
            } else {
              ((YamcsServer) selectedObject).disconnect();
            }
          }
        });
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

    MenuItem editServer = new MenuItem("Edit Server");
    editServer.setOnAction(
        e -> {
          TreeItem<YamcsObject<?>> selectedItem = treeView.getSelectionModel().getSelectedItem();

          if (selectedItem == null) {

          } else {
            // TODO: Should not be necessary to check the object type in this case...
            if (selectedItem.getValue().getObjectType().equals(YamcsServer.OBJECT_TYPE)) {
              Callback<YamcsServerConnection, Boolean> callback =
                  new Callback<YamcsServerConnection, Boolean>() {

                    @Override
                    public Boolean call(YamcsServerConnection connection) {
                      return YamcsServer.testConnection(connection);
                    }
                  };
              String oldServerName =
                  ((YamcsServer) selectedItem.getValue()).getConnection().getName();
              EditConnectionDialog editDialog =
                  new EditConnectionDialog(
                      callback, ((YamcsServer) selectedItem.getValue()).getConnection(), "");
              YamcsServerConnection newServer = editDialog.showAndWait().orElse(null);
              if (newServer == null) return;

              YamcsObjectManager.setConnectionObjForServer(
                  newServer, oldServerName, newServer.getName());
            } else {

            }
          }
        });

    // add menu items to menu
    contextMenu.getItems().add(addServer);
    contextMenu.getItems().add(removeServer);
    contextMenu.getItems().add(sep);
    contextMenu.getItems().add(connectAllServers);
    contextMenu.getItems().add(disconnectAllServers);
    contextMenu.getItems().add(connectServer);
    contextMenu.getItems().add(disconnectServer);
    contextMenu.getItems().add(setAsDefault);
    contextMenu.getItems().add(editServer);

    // setContextMenu to label
    treeView.setContextMenu(contextMenu);

    treeView.setOnContextMenuRequested(
        e -> {
          TreeItem<YamcsObject<?>> selectedItem = treeView.getSelectionModel().getSelectedItem();

          if (selectedItem == null) {
            /* Nothing is selected. */
            addServer.setDisable(false);
            removeServer.setDisable(true);
            connectAllServers.setDisable(false);
            disconnectAllServers.setDisable(false);
            connectServer.setDisable(true);
            disconnectServer.setDisable(true);
            setAsDefault.setDisable(true);
            editServer.setDisable(true);
            connectAllServers.setDisable(true);
            disconnectAllServers.setDisable(true);

          } else {
            YamcsObject<?> selectedObject = selectedItem.getValue();

            if (selectedObject.getObjectType() == "root") {
              /* This is the root node. This shouldn't be possible. */
              addServer.setDisable(true);
              removeServer.setDisable(true);
              connectAllServers.setDisable(true);
              disconnectAllServers.setDisable(true);
              connectServer.setDisable(true);
              disconnectServer.setDisable(true);
              setAsDefault.setDisable(true);
              editServer.setDisable(true);
            } else if (selectedObject.getObjectType() == YamcsServer.OBJECT_TYPE) {
              /* This is a server node. */
              addServer.setDisable(false);
              removeServer.setDisable(false);
              connectAllServers.setDisable(false);
              disconnectAllServers.setDisable(false);
              if (((YamcsServer) selectedObject)
                  .getServerState()
                  .equals(ConnectionState.DISCONNECTED)) {
                connectServer.setDisable(false);
                disconnectServer.setDisable(true);
                editServer.setDisable(false);
              } else {
                connectServer.setDisable(true);
                disconnectServer.setDisable(false);
                editServer.setDisable(true);
              }
              setAsDefault.setDisable(true);
            } else if (selectedObject.getObjectType() == CMDR_YamcsInstance.OBJECT_TYPE) {
              /* This is a instance node. */
              addServer.setDisable(true);
              removeServer.setDisable(true);
              connectAllServers.setDisable(true);
              disconnectAllServers.setDisable(true);
              connectServer.setDisable(true);
              disconnectServer.setDisable(true);

              if (YamcsObjectManager.getDefaultInstance() != null
                  && YamcsObjectManager.getDefaultInstance().equals(selectedObject)) {
                setAsDefault.setDisable(true);
              } else {
                setAsDefault.setDisable(false);
              }

              editServer.setDisable(true);
            } else {
              /* I don't know what this is. */
              addServer.setDisable(false);
              removeServer.setDisable(false);
              connectAllServers.setDisable(false);
              disconnectAllServers.setDisable(false);
              connectServer.setDisable(false);
              disconnectServer.setDisable(false);
              setAsDefault.setDisable(false);
              editServer.setDisable(false);
            }
          }
        });
  }

  private void attemptToConnect(YamcsServer s) {
    if (s.getServerState().equals(ConnectionState.DISCONNECTED)) {
      if (!s.connect()) {
        Alert errorDialog = new Alert(AlertType.ERROR);
        errorDialog.setContentText("Failed to connect to:" + "\"" + s.getConnection() + "\"");
        errorDialog.showAndWait();
      }
    } else {
      // Should never happen
    }
  }

  public TreeView<YamcsObject<?>> getTreeView() {
    return treeView;
  }

  private TreeItem<YamcsObject<?>> createItem(YamcsObject<?> object) {

    // create tree item with children from game object's list:

    TreeItem<YamcsObject<?>> item = new TreeItem<>(object);
    item.setExpanded(true);
    YamcsAware listener = new YamcsAware() {
          //      onYamcsConnectionFailed
        };
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
