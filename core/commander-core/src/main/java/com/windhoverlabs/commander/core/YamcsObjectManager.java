package com.windhoverlabs.commander.core;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import java.util.ArrayList;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Very simple class which manages YamcsObjects. Users of commander-core should always use this
 * class to access YamcsObject objects via the getRoot() method.
 *
 * @author lgomez
 */
public final class YamcsObjectManager {

  /** Logger for all file browser code */
  public static final Logger log = Logger.getLogger(YamcsObjectManager.class.getPackageName());

  private static YamcsObject<YamcsServer> root;
  private static ObservableList<YamcsServer> servers = FXCollections.observableArrayList();

  public static void setConnectionObjForServer(
      YamcsServerConnection newConnection, String oldServerName, String newServerName) {
    getServerFromName(oldServerName).setConnection(newConnection);
    if (defaultServerName != null && defaultServerName.equals(oldServerName)) {
      defaultServerName = newServerName;
    }
  }

  public static ObservableList<YamcsServer> getServers() {
    return servers;
  }

  private static CMDR_YamcsInstance defaultInstance = null;
  private static String defaultInstanceName = null;
  private static String defaultServerName = null;

  public static String getDefaultServerName() {
    return defaultServerName;
  }

  public static String getDefaultInstanceName() {
    return defaultInstanceName;
  }

  // At the moment we do not support setting a default server directly by the outside
  private static YamcsServer defaultServer = null;

  private static ArrayList<YamcsAware> listeners = new ArrayList<YamcsAware>();
  private static ArrayList<YamcsObjectManagerAware> managerListeners =
      new ArrayList<YamcsObjectManagerAware>();

  public static YamcsServer getDefaulServer() {
    return defaultServer;
  }

  public static CMDR_YamcsInstance getDefaultInstance() {
    return defaultInstance;
  }

  public static void setDefaultInstance(String server, String instance) {
    defaultServer = getServerFromName(server);
    if (defaultServer == null) {
      log.warning("Server " + "\"" + server + "\" not found");
      return;
    }

    for (YamcsServer s : root.getItems()) {
      s.setDefaultInstance(null);
    }
    defaultInstanceName = instance;
    defaultServerName = server;
    defaultInstance = getServerFromName(server).getInstance(instance);
    if (defaultInstance == null) {
      // Should not happen.
      log.warning("Instance " + "\"" + instance + "\" not found");
      return;
    }
    defaultServer.setDefaultInstance(defaultInstanceName);

    if (defaultInstance != null) {
      for (YamcsAware listener : listeners) {
        listener.changeDefaultInstance();
      }
    }
  }

  private YamcsObjectManager() {}

  public static YamcsObject<YamcsServer> getRoot() {
    return root;
  }

  static {
    root =
        new YamcsObject<YamcsServer>("") {

          @Override
          public String getObjectType() {
            return "root";
          }

          @Override
          public ObservableList<YamcsServer> getItems() {
            return servers;
          }

          @Override
          public void createAndAddChild(String name) {
            YamcsServer newServer = new YamcsServer(name);
            try {
              for (YamcsAware l : listeners) {
                newServer.addListener(l);
              }
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            getItems().add(newServer);
          }
        };
  }

  public static void addYamcsListener(YamcsAware newListener) {
    listeners.add(newListener);
  }

  public static void addManagerListener(YamcsObjectManagerAware newListener) {
    managerListeners.add(newListener);
  }
  /**
   * Traverse through allServers and find the server object that matches name
   *
   * @param name Name of the user-defined server.
   * @return The object with the server name.
   */
  public static YamcsServer getServerFromName(String name) {
    YamcsServer outServer = null;
    for (YamcsObject<?> server : root.getItems()) {
      if (server.getName().equals(name)) {
        outServer = (YamcsServer) server;
      }
    }
    return outServer;
  }

  /**
   * Traverse through allServers and find the instance object that matches pathToInstance
   *
   * @param serverName Name of the user-defined server. "Server_A:yamcs-cfs"
   * @return The object with the server name.
   */
  public static CMDR_YamcsInstance getInstanceFromName(String serverName, String instanceName) {
    CMDR_YamcsInstance outServer = null;
    for (YamcsObject<?> server : root.getItems()) {
      if (server.getName().equals(serverName)) {
        outServer = ((YamcsServer) server).getInstance(instanceName);
      }
    }
    return outServer;
  }
}
