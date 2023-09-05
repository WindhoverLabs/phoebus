package com.windhoverlabs.yamcs.core;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import com.windhoverlabs.pv.yamcs.YamcsAware.YamcsAwareMethod;
import java.util.ArrayList;
import java.util.logging.Logger;
import javafx.application.Platform;
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

  // At the moment we do not support setting a default server directly by the outside
  private static YamcsServer defaultServer = null;

  private static ArrayList<YamcsAware> listeners = new ArrayList<YamcsAware>();

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
              YamcsAware managerListener =
                  new YamcsAware() {
                    //    	TODO:At the moment onYamcsConnected is not being called.
                    // changeDefaultInstance is
                    // driving
                    //    	the listeners for now.
                    public void onYamcsConnected() {
                      //            managerStatus.set("Connection Status: Connected");
                      System.out.println(
                          "************onYamcsConnected, managerStatus************************");
                    }

                    public void onYamcsDisconnected() {
                      //            managerStatus.set("Connection Status: Disconnected");
                      System.out.println(
                          "************changeDefaultInstance, managerStatus************************");
                    }

                    public void changeDefaultInstance() {
                      System.out.println(
                          "************changeDefaultInstance, managerStatus************************");
                      Platform.runLater(
                          () -> {
                            managerStatus.set(
                                "Connection Status: Connected. Default set to"
                                    + getDefaultInstanceName());
                          });
                      ;
                    }
                  };
              //              newServer.addListener(managerListener);
              listeners.add(managerListener);
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

  static void triggreYamcsListeners(YamcsAwareMethod m) {
    for (YamcsAware l : listeners) {
      switch (m) {
        case onYamcsDisconnected:
          {
            l.onYamcsDisconnected();
          }
          break;
        case changeDefaultInstance:
          System.out.println(
              "changeDefaultInstance$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$4444");
          l.changeDefaultInstance();
          ;
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
}
