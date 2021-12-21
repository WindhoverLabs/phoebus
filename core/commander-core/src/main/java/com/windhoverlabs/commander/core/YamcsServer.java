package com.windhoverlabs.commander.core;

import org.yamcs.client.ClientException;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.YamcsInstance;

public class YamcsServer extends YamcsObject<CMDR_YamcsInstance> {
  public static String OBJECT_TYPE = "server";
  private YamcsClient yamcsClient;
  private YamcsServerConnection connection;

  private boolean isConnected;

  public boolean isConnected() {
    return isConnected;
  }

  public YamcsServer(String name) {
    super(name);
  }

  @Override
  public void createAndAddChild(String name) {
    getItems().add(new CMDR_YamcsInstance(name));
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  public void connect(YamcsServerConnection newConnection) {
    connection = newConnection;
    if (yamcsClient != null) {
      yamcsClient.close();
    }
    yamcsClient = YamcsClient.newBuilder(connection.getUrl(), connection.getPort()).build();

    yamcsClient
        .listInstances()
        .whenComplete(
            (response, exc) -> {
              if (exc == null) {
                for (YamcsInstance instance : response) {
                  createAndAddChild(instance.getName());
                  getItems().get(getItems().size() - 1).initProcessorClient(yamcsClient);
                  getItems()
                      .get(getItems().size() - 1)
                      .initYamcsSubscriptionService(yamcsClient, this.getName());
                  getItems()
                      .get(getItems().size() - 1)
                      .initEventSubscription(yamcsClient, this.getName());
                }
              }
            });

    try {
      yamcsClient.connectWebSocket();

    } catch (ClientException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    }

    isConnected = true;
  }

  public YamcsServerConnection getConnection() {
    return connection;
  }

  public CMDR_YamcsInstance getInstance(String instanceName) {
    CMDR_YamcsInstance resultInstance = null;
    for (CMDR_YamcsInstance instance : getItems()) {
      if (instanceName.equals(instance.getName())) {
        resultInstance = instance;
        break;
      }
    }

    return resultInstance;
  }
}
