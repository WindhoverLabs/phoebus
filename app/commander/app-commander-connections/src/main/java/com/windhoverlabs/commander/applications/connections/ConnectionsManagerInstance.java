package com.windhoverlabs.commander.applications.connections;

import com.windhoverlabs.commander.core.YamcsObject;
import com.windhoverlabs.commander.core.YamcsObjectManager;
import com.windhoverlabs.commander.core.YamcsServer;
import com.windhoverlabs.commander.core.YamcsServerConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.phoebus.framework.persistence.Memento;
import org.phoebus.framework.persistence.MementoTree;
import org.phoebus.framework.persistence.XMLMementoTree;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.framework.workbench.Locations;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;

/** @author lgomez */
@SuppressWarnings("nls")
public class ConnectionsManagerInstance implements AppInstance {
  private static final String YAMCS_CONNECTIONS_MEMENTO_FILENAME = "yamcs_connections_memento";

  /** Logger for all file browser code */
  public static final Logger logger =
      Logger.getLogger(ConnectionsManagerInstance.class.getPackageName());

  /** Memento tags */
  private static final String YAMCS_CONNECTIONS = "yamcs_connections",
      YAMCS_URL = "url",
      YAMCS_PORT = "port",
      YAMCS_CONNECTION_NAME = "name",
      YAMCS_DEFAULT_INSTANCE = "default_instance";

  static ConnectionsManagerInstance INSTANCE = null;

  private final AppDescriptor app;

  // TODO: Refactor Tree constructor since we don't need to pass the list of servers anymore.
  private static Tree serverTree = new Tree(restoreServers());

  private DockItem tab = null;

  public ConnectionsManagerInstance(AppDescriptor app) {
    this.app = app;

    Node content;
    content = serverTree.getTreeView();

    tab = new DockItem(this, content);
    DockPane.getActiveDockPane().addTab(tab);
    tab.addCloseCheck(
        () -> {
          INSTANCE = null;
          return CompletableFuture.completedFuture(true);
        });
  }

  @Override
  public AppDescriptor getAppDescriptor() {
    return app;
  }

  @Override
  public void restore(final Memento memento) {
    // TODO: Move "new Tree(restoreServers());" here.
  }

  @Override
  public void save(final Memento memento) {
    try {
    } catch (Exception e) {
      logger.warning("Error saving Yamcs connections...:" + e.toString());
    }

    // Save yamcs connections
    try {
      createYamcsConnectionMemento();
    } catch (Exception ex) {
      logger.log(Level.WARNING, "Error writing saved state to " + "", ex);
    }
  }

  public static void createYamcsConnectionMemento() throws Exception, FileNotFoundException {
    logger.info("Saving Yamcs connections...");
    final XMLMementoTree yamcsConnectionsMemento = XMLMementoTree.create();
    yamcsConnectionsMemento.createChild(YAMCS_CONNECTIONS);

    YamcsObject<YamcsServer> treeRoot = serverTree.getRoot();

    for (YamcsServer s : treeRoot.getItems()) {
      yamcsConnectionsMemento.getChild(YAMCS_CONNECTIONS).createChild(s.getConnection().getName());

      MementoTree connection =
          yamcsConnectionsMemento.getChild(YAMCS_CONNECTIONS).getChild(s.getConnection().getName());

      connection.setString(YAMCS_URL, s.getConnection().getUrl());
      connection.setString(YAMCS_PORT, Integer.toString(s.getConnection().getPort()));
      connection.setString(YAMCS_CONNECTION_NAME, s.getName());
      if (s.getDefaultInstance() != null) {
        connection.setString(YAMCS_DEFAULT_INSTANCE, s.getDefaultInstance().getName());
      }
    }

    yamcsConnectionsMemento.write(
        new FileOutputStream(new File(Locations.user(), YAMCS_CONNECTIONS_MEMENTO_FILENAME)));
  }

  public void raise() {
    tab.select();
  }

  private static ObservableList<YamcsServer> restoreServers() {
    ObservableList<YamcsServer> serverList = YamcsObjectManager.getRoot().getItems();

    try {

      final XMLMementoTree yamcsConnectionsMemento =
          XMLMementoTree.read(
              new FileInputStream(new File(Locations.user(), YAMCS_CONNECTIONS_MEMENTO_FILENAME)));

      for (MementoTree child : yamcsConnectionsMemento.getChild(YAMCS_CONNECTIONS).getChildren()) {
        // TODO: child.getString(YAMCS_CONNECTION_NAME) should never be null.
        YamcsServer server = new YamcsServer(child.getString(YAMCS_CONNECTION_NAME).orElse(null));
        server.connect(
            new YamcsServerConnection(
                child.getString(YAMCS_CONNECTION_NAME).orElse(null),
                child.getString(YAMCS_URL).orElse(null),
                Integer.parseInt(child.getString(YAMCS_PORT).orElse(null))));
        // TODO:Probably not the best way of doing this...
        server.setDefaultInstance(child.getString(YAMCS_DEFAULT_INSTANCE).orElse(null));
        serverList.add(server);
      }
    } catch (Exception e) {
      logger.warning("Error restoring yamcs servers:" + e);
    }

    return serverList;
  }

  public static Tree getServerTree() {
    return serverTree;
  }
}
