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
  private static CMDR_YamcsInstance defaultInstance = null;
  // At the moment we do not support setting a default server directly by the outside
  private static YamcsServer defaultServer = null;

  private static ArrayList<YamcsAware> listeners = new ArrayList<YamcsAware>();

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
    YamcsObjectManager.defaultInstance = getServerFromName(server).getInstance(instance);
    defaultServer.setDefaultInstance(YamcsObjectManager.defaultInstance.getName());
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
