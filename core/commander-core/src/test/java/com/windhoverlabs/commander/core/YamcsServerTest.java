package com.windhoverlabs.commander.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.yamcs.client.ClientException;

public class YamcsServerTest extends AbstractIntegrationTest {
  private YamcsServer newServer;
  YamcsServerConnection newConnection;

  @Override
  @BeforeEach
  public void before() throws ClientException {
    super.before();
    newServer = new YamcsServer("sitl");
    assertThat("The connection name is equal to \"sitl\"", newServer.getName(), equalTo("sitl"));
    assertThat(
        "newServer is of type \"server\".",
        newServer.getObjectType(),
        equalTo(YamcsServer.OBJECT_TYPE));
    newConnection = new YamcsServerConnection("sitl", "localhost", 9190, "admin", "rootpassword");

    assertThat("", newConnection.getName(), equalTo("sitl"));
  }

  @BeforeAll
  public static void initYamcs() throws Exception {
    //    assertThat("", );
    setupYamcs();
  }

  @Test
  @BeforeAll
  @Order(1)
  public static void testYamcsServerConnection() {
    YamcsServerConnection newConnection =
        new YamcsServerConnection("sitl", "localhost", 9190, "admin", "rootpassword");

    assertThat("", newConnection.getName(), equalTo("sitl"));
    assertThat("", newConnection.getUrl(), equalTo("localhost"));
    assertThat("", newConnection.getPort(), equalTo(9190));
    assertThat("", newConnection.getUser(), equalTo("admin"));
    assertThat("", newConnection.getPassword(), equalTo("rootpassword"));
    assertThat("", newConnection.toString(), equalTo("localhost:9190"));

    newConnection.setName("sitl-new");
    assertThat("", newConnection.getName(), equalTo("sitl-new"));
    newConnection.setUrl("localhost-new");
    assertThat("", newConnection.getUrl(), equalTo("localhost-new"));
    newConnection.setPort(1234);
    assertThat("", newConnection.getPort(), equalTo(1234));
    newConnection.setUser("admin-new");
    assertThat("", newConnection.getUser(), equalTo("admin-new"));

    newConnection = new YamcsServerConnection("sitl", "localhost", 9190);

    assertThat("", newConnection.getName(), equalTo("sitl"));
    assertThat("", newConnection.getUrl(), equalTo("localhost"));
    assertThat("", newConnection.getPort(), equalTo(9190));
    assertThat("", newConnection.getUser(), equalTo(null));
    assertThat("", newConnection.getPassword(), equalTo(null));

    newConnection = new YamcsServerConnection("sitl", "localhost", 9190, "Captain_Tom");

    assertThat("", newConnection.getName(), equalTo("sitl"));
    assertThat("", newConnection.getUrl(), equalTo("localhost"));
    assertThat("", newConnection.getPort(), equalTo(9190));
    assertThat("", newConnection.getUser(), equalTo("Captain_Tom"));
    assertThat("", newConnection.getPassword(), equalTo(null));

    newConnection =
        new YamcsServerConnection(
            new YamcsServerConnection("sitl", "localhost", 9190, "Captain_Tom"));

    assertThat("", newConnection.getName(), equalTo("sitl"));
    assertThat("", newConnection.getUrl(), equalTo("localhost"));
    assertThat("", newConnection.getPort(), equalTo(9190));
    assertThat("", newConnection.getUser(), equalTo("Captain_Tom"));
    assertThat("", newConnection.getPassword(), equalTo(null));
  }

  @Test
  @Order(2)
  public void testYamcsServersetConnectionName() {
    newServer.setConnection(newConnection);

    assertThat(
        "The new connection object is equal to what was passed in setConnection",
        newServer.getConnection(),
        equalTo(newConnection));
  }

  @Test
  @Order(3)
  public void testYamcsServerConnect() throws InterruptedException, ExecutionException {
    newServer.connect(newConnection);
    assertThat(
        "YamcsServer is connected", newServer.getServerState(), equalTo(ConnectionState.CONNECTED));

    newServer.disconnect();

    assertThat(
        "YamcsServer is disconnected",
        newServer.getServerState(),
        equalTo(ConnectionState.DISCONNECTED));
  }

  @Test
  @Order(4)
  public void testYamcsServerConnectNoArgs() throws InterruptedException, ExecutionException {
    newServer.setConnection(newConnection);
    newServer.connect();
    assertThat(
        "YamcsServer is connected", newServer.getServerState(), equalTo(ConnectionState.CONNECTED));

    newServer.disconnect();

    assertThat(
        "YamcsServer is disconnected",
        newServer.getServerState(),
        equalTo(ConnectionState.DISCONNECTED));
  }

  @BeforeAll
  public static void testYamcsServerTestConnection()
      throws InterruptedException, ExecutionException {
    YamcsServerConnection newConnection =
        new YamcsServerConnection("sitl", "localhost", 9190, "admin", "rootpassword");

    assertThat(
        "connection test is successful", YamcsServer.testConnection(newConnection), equalTo(true));

    newConnection =
        new YamcsServerConnection("sitl", "not_localhost", 9190, "admin", "rootpassword");

    assertThat(
        "connection test is not successful",
        YamcsServer.testConnection(newConnection),
        equalTo(false));
  }

  @Test
  @Order(5)
  public void testYamcsServerInstances() throws InterruptedException, ExecutionException {
    newServer.connect(newConnection);
    assertThat(
        "YamcsServer is connected", newServer.getServerState(), equalTo(ConnectionState.CONNECTED));
    CompletableFuture<String> future = new CompletableFuture<>();

    ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1);
    executorService.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              // TODO:Not sure if this the best way to do this
              Thread.sleep(100);
              assertThat("1 yamcs instance exists", newServer.getItems().size(), equalTo(1));
              future.complete("Success");
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        });

    assertThat("future is successful", future.get(), equalTo("Success"));
    assertThat(
        "YamcsServer instance is not null", newServer.getInstance(yamcsInstance), notNullValue());
    assertThat(
        "yamcs instance is equal to YamcsServer item",
        newServer.getItems().get(0),
        equalTo(newServer.getInstance(yamcsInstance)));
    assertThat(
        "yamcs instance name is equal to YamcsServer item name",
        newServer.getInstance(yamcsInstance).getName(),
        equalTo(newServer.getItems().get(0).getName()));
    assertThat("Default instance is null", newServer.getDefaultInstance(), nullValue());
  }

  @AfterEach
  @Override
  public void after() throws InterruptedException {
    super.after();
  }
}
