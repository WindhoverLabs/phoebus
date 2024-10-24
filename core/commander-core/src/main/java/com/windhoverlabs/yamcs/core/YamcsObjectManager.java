package com.windhoverlabs.yamcs.core;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.pv.yamcs.YamcsAware.YamcsAwareMethod;
import java.util.ArrayList;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
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
  private YamcsAware listener;

  private static SimpleStringProperty managerStatus =
      new SimpleStringProperty("Connection Status: Disconnected");

  public static SimpleStringProperty getManagerStatus() {
    return managerStatus;
  }

  private static ArrayList<YamcsAware> listeners = new ArrayList<YamcsAware>();

  //  TODO: Really don't like this static block pattern. The proper way to do it is to add listeners
  // to the "servers" observable list.
  //  This will do, for now.

  static {
  }

  // At the moment we do not support setting a default server directly by the outside
  private static YamcsServer defaultServer = null;
  // TODO:When doing integration testing(specifically Connections App), refactor this
  // such that third parameter is removed.
  public static void setConnectionObjForServer(
      YamcsServerConnection newConnection, String oldServerName, String newServerName) {
    getServerFromName(oldServerName).setConnection(newConnection);
    if (defaultServerName != null && defaultServerName.equals(oldServerName)) {
      defaultServerName = newServerName;
    }
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

  public static YamcsServer getDefaultServer() {
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
      log.warning("Should not happen: Instance " + "\"" + instance + "\" not found");
      return;
    }
    defaultServer.setDefaultInstance(defaultInstanceName);

    if (defaultInstance != null) {
      for (YamcsAware listener : listeners) {
        listener.changeDefaultInstance();
      }
    }
  }

  private YamcsObjectManager() {

    //    addYamcsListener(listener);
  }

  public static YamcsObject<YamcsServer> getRoot() {
    return root;
  }

  static {
    YamcsAware managerListener =
        new YamcsAware() {
          /**
           * TODO:At the moment onYamcsConnected is not being called changeDefaultInstance is
           * driving the listeners for now.
           */
          public void onYamcsConnected() {
            if (!anyServerConnected()) {
              managerStatus.set("Connection Status: Connected");
            }
          }

          public void onYamcsDisconnected() {
            if (!anyServerConnected()) {
              managerStatus.set("Connection Status: Disconnected");
            }
          }

          public void changeDefaultInstance() {
            if (anyServerConnected()) {
              managerStatus.set("Connection Status: Connected");
              //            	TODO:Keep this simple for now, need to think about this logic a bit
              // more.
              //              managerStatus.set(
              //                  "Connection Status: Connected. Default Server: \""
              //                      + getDefaultServerName()
              //                      + "\""
              //                      + ". Default set to "
              //                      + "\""
              //                      + getDefaultInstanceName()
              //                      + "\"");
            }
          }

          public void onInstancesReady(YamcsServer s) {
            managerStatus.set("Connection Status: Connected to \"" + s.getName() + "\"");
          }
        };
    listeners.add(managerListener);
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
    log.info("addYamcsListener on YamcsServer");
    listeners.add(newListener);
    if (defaultInstance != null) {
      newListener.changeDefaultInstance();
    }
    //  for (YamcsServer s : YamcsObjectManager.getRoot().getItems()) {
    //  s.addListener(yamcsListener);
    // }
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
  //  TODO:Right now there are several ways to "trigger" these events. I think this function should
  // become the only and default way
  //  to avoid redundant code paths that maintenance more difficult and makes code harder to follow.
  static void triggerYamcsListeners(YamcsAwareMethod m) {
    for (YamcsAware l : listeners) {
      switch (m) {
        case onYamcsDisconnected:
          {
            l.onYamcsDisconnected();
          }
          break;
        case changeDefaultInstance:
          l.changeDefaultInstance();
          ;
          break;
        case onYamcsConnected:
          {
            l.onYamcsConnected();
          }
          break;
        default:
          break;
      }
    }
  }

  public static void removeListener(YamcsAware l) {
    listeners.remove(l);
  }

  public static void switchProcessor(String processorName) {
    if (defaultInstance != null) {
      for (YamcsAware listener : listeners) {
        getDefaultInstance()
            .switchProcessor(
                getDefaultServer().getYamcsClient(), getDefaultServer().getName(), processorName);
        listener.changeProcessor(getDefaultInstance().getName(), processorName);
      }
    }
  }

  private static boolean anyServerConnected() {
    boolean isConnected = false;
    for (var s : servers) {
      if (s.getServerState() == ConnectionState.CONNECTED) {
        isConnected = true;
      }
    }

    return isConnected;
  }
}
