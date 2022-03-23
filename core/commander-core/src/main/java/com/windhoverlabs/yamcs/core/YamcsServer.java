package com.windhoverlabs.yamcs.core;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import java.util.ArrayList;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.yamcs.client.ClientException;
import org.yamcs.client.ConnectionListener;
import org.yamcs.client.InstanceFilter;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.YamcsInstance;
import org.yamcs.protobuf.YamcsInstance.InstanceState;

public class YamcsServer extends YamcsObject<CMDR_YamcsInstance> {

  public static final Logger log = Logger.getLogger(YamcsServer.class.getPackageName());

  public static String OBJECT_TYPE = "server";
  private YamcsClient yamcsClient;
  private CMDR_YamcsInstance defaultInstance = null;
  // TODO: It's starting to look like this class and YamcsObjectManager should be the same class...

  private ConnectionState serverState = ConnectionState.DISCONNECTED;
  private ArrayList<YamcsAware> listeners = new ArrayList<YamcsAware>();
  private StringProperty serverStateStrProperty = new SimpleStringProperty();

  public StringProperty getServerStateStrProperty() {
    return serverStateStrProperty;
  }

  public YamcsClient getYamcsClient() {
    return yamcsClient;
  }

  private YamcsServerConnection connection;

  public void setConnection(YamcsServerConnection connection) {
    if (serverState == ConnectionState.DISCONNECTED) {
      this.connection = connection;
      this.setName(connection.getName());
      serverStateStrProperty.set(this.toString());
    }
  }

  public YamcsServer(String name) {
    super(name);
    serverStateStrProperty.set(this.toString());
  }

  @Override
  public void createAndAddChild(String name) {
    getItems().add(new CMDR_YamcsInstance(name));
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  public void addListener(YamcsAware newListener) {
    listeners.add(newListener);
  }

  public boolean connect(YamcsServerConnection newConnection) {
    connection = newConnection;

    // TODO:Not sure if this is necessary given our non-global model of instances
    if (yamcsClient != null) {
      yamcsClient.close();
    }
    yamcsClient = YamcsClient.newBuilder(connection.getUrl(), connection.getPort()).build();

    if (newConnection.getPassword() != null && newConnection.getUser() != null) {
      try {
        yamcsClient.login(newConnection.getUser(), newConnection.getPassword().toCharArray());
      } catch (ClientException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return false;
      }
    }

    InstanceFilter filter = new InstanceFilter();
    filter.setState(InstanceState.RUNNING);
    yamcsClient
        .listInstances(filter)
        .whenComplete(
            (response, exc) -> {
              if (exc == null) {
                for (YamcsInstance instance : response.getInstancesList()) {
                  createAndAddChild(instance.getName());

                  // TODO:Don't really like doing this here...We should either make
                  // YamcsObjectManager
                  // package-private or move all of the code from YamcsObjectManager to this class.
                  //                  if (YamcsObjectManager.getDefaultInstance() != null
                  //                      && YamcsObjectManager.getDefaultInstance()
                  //                          .getName()
                  //                          .equals(instance.getName())) {
                  //                    YamcsObjectManager.setDefaultInstance(
                  //                        getName(),
                  // YamcsObjectManager.getDefaultInstance().getName());
                  //                  }

                  getItems().get(getItems().size() - 1).activate(yamcsClient, getName());

                  for (YamcsAware l : listeners) {
                    l.onInstancesReady();
                  }
                }
              }
            });

    try {
      yamcsClient.addConnectionListener(
          new ConnectionListener() {

            @Override
            public void connecting() {
              // TODO Call of the subscribers
            }

            @Override
            public void connected() {
              init();
            }

            @Override
            public void disconnected() {
              disconnect();
            }

            @Override
            public void connectionFailed(Throwable cause) {
              log.warning("Failed to connect to " + getName());
            }
          });

      yamcsClient.connectWebSocket();
      serverState = ConnectionState.CONNECTED;
    } catch (ClientException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }

    serverStateStrProperty.set(this.toString());

    return true;
  }

  public boolean connect() {
    // TODO:Not sure if this is necessary given our non-global model of instances
    if (yamcsClient != null) {
      yamcsClient.close();
    }
    yamcsClient = YamcsClient.newBuilder(connection.getUrl(), connection.getPort()).build();

    if (connection.getPassword() != null && connection.getUser() != null) {
      try {
        yamcsClient.login(connection.getUser(), connection.getPassword().toCharArray());
      } catch (ClientException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return false;
      }
    }

    InstanceFilter filter = new InstanceFilter();
    filter.setState(InstanceState.RUNNING);
    yamcsClient
        .listInstances(filter)
        .whenComplete(
            (response, exc) -> {
              if (exc == null) {
                for (YamcsInstance instance : response.getInstancesList()) {
                  createAndAddChild(instance.getName());

                  // TODO:Don't really like doing this here...We should either make
                  // YamcsObjectManager
                  // package-private or move all of the code from YamcsObjectManager to this class.
                  if (YamcsObjectManager.getDefaultInstanceName() != null
                      && YamcsObjectManager.getDefaultInstanceName().equals(instance.getName())
                      && YamcsObjectManager.getDefaultServerName().equals(this.getName())) {
                    YamcsObjectManager.setDefaultInstance(getName(), instance.getName());
                  }

                  getItems().get(getItems().size() - 1).activate(yamcsClient, getName());

                  for (YamcsAware l : listeners) {
                    l.onInstancesReady();
                  }
                }
              }
            });

    try {
      yamcsClient.addConnectionListener(
          new ConnectionListener() {

            @Override
            public void connecting() {
              // TODO Call of the subscribers
            }

            @Override
            public void connected() {
              init();
            }

            @Override
            public void disconnected() {
              disconnect();
            }

            @Override
            public void connectionFailed(Throwable cause) {
              log.warning("Failed to connect to " + getName());
            }
          });
      yamcsClient.connectWebSocket();

    } catch (ClientException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }

    return true;
  }

  private void unInit() {
    for (CMDR_YamcsInstance instance : this.getItems()) {
      instance.deActivate(yamcsClient, this.getName());
    }
    this.getItems().clear();
    serverState = ConnectionState.DISCONNECTED;
    serverStateStrProperty.set(this.toString());
  }

  private void init() {
    // TODO: Have to think about this one for a minute
    //    for (CMDR_YamcsInstance instance : this.getItems()) {
    //      instance.activate(yamcsClient, this.getName());
    //    }
    serverState = ConnectionState.CONNECTED;
    serverStateStrProperty.set(this.toString());
  }

  public void disconnect() {
    // TODO: unInit resources such as event subscriptions, parameter subscriptions, etc
    if (yamcsClient != null) {
      yamcsClient.close();
    }
    unInit();
  }

  public YamcsServerConnection getConnection() {
    return connection;
  }

  public CMDR_YamcsInstance getInstance(String instanceName) {
    CMDR_YamcsInstance resultInstance = null;

    if (instanceName != null) {
      for (CMDR_YamcsInstance instance : getItems()) {
        if (instanceName.equals(instance.getName())) {
          resultInstance = instance;
          break;
        }
      }
    }

    return resultInstance;
  }

  void setDefaultInstance(String instanceName) {
    defaultInstance = getInstance(instanceName);
  }

  public CMDR_YamcsInstance getDefaultInstance() {
    return defaultInstance;
  }

  public ConnectionState getServerState() {
    return serverState;
  }

  /**
   * Attempt to connect.
   *
   * @param newConnection
   * @return true if the connection attempt is successful. Otherwise, this function returns false.
   */
  public static boolean testConnection(YamcsServerConnection newConnection) {

    YamcsClient yamcsClient = null;
    try {
      yamcsClient = YamcsClient.newBuilder(newConnection.getUrl(), newConnection.getPort()).build();

      if (newConnection.getPassword() != null && newConnection.getUser() != null) {

        yamcsClient.login(newConnection.getUser(), newConnection.getPassword().toCharArray());
      }

      yamcsClient.connectWebSocket();

    } catch (Exception e) {
      // TODO Auto-generated catch block
      return false;
    }

    yamcsClient.close();

    return true;
  }

  public final String toString() {
    return getName() + " | " + getServerState();
  }
}
