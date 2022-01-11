package com.windhoverlabs.commander.core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Very simple class which manages YamcsObjects. Users of commander-core should always use this
 * class to access YamcsObject objects via the getRoot() method.
 *
 * @author lgomez
 */
public final class YamcsObjectManager {
  private static YamcsObject<YamcsServer> root;
  private static ObservableList<YamcsServer> servers = FXCollections.observableArrayList();
  private static YamcsServer defaultServer = null;

  public static YamcsServer getDefaultServer() {
    return defaultServer;
  }

  public static void setDefaultServer(String defaultServer) {
    YamcsObjectManager.defaultServer = getServerFromName(defaultServer);
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
            getItems().add(new YamcsServer(name));
          }
        };
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
