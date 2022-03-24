package com.windhoverlabs.yamcs.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.windhoverlabs.pv.yamcs.YamcsAware;
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
  public void testYamcsServersetConnection() {
    newServer.setConnection(newConnection);

    assertThat(
        "The new connection object is equal to what was passed in setConnection",
        newServer.getConnection(),
        equalTo(newConnection));
  }

  @Test
  @Order(3)
  public void testYamcsServerConnect() throws InterruptedException, ExecutionException {
    assertThat(
        "", newServer.getServerStateStrProperty().get(), equalTo("sitl" + " | " + "DISCONNECTED"));
    newServer.connect(newConnection);
    assertThat(
        "YamcsServer is connected", newServer.getServerState(), equalTo(ConnectionState.CONNECTED));

    assertThat("", newServer.getYamcsClient(), notNullValue());

    assertThat(
        "", newServer.getServerStateStrProperty().get(), equalTo("sitl" + " | " + "CONNECTED"));
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

  @Test
  public void testYamcsServerNoArgsConnectIncorrect()
      throws InterruptedException, ExecutionException {
    assertThat("", newConnection.getName(), equalTo("sitl"));
    assertThat("", newConnection.getUrl(), equalTo("localhost"));
    assertThat("", newConnection.getPort(), equalTo(9190));
    assertThat("", newConnection.getUser(), equalTo("admin"));
    assertThat("", newConnection.getPassword(), equalTo("rootpassword"));
    assertThat("", newConnection.toString(), equalTo("localhost:9190"));
    assertThat(
        "YamcsServer is not connected",
        newServer.getServerState(),
        equalTo(ConnectionState.DISCONNECTED));
    newConnection.setUser("not_an_admin");
    newServer.setConnection(newConnection);

    assertThat(
        "YamcsServer connection is equal to what was passed to newConnection",
        newServer.getConnection(),
        equalTo(newConnection));
    assertThat("Attempt to connect fails", newServer.connect(), equalTo(false));
    assertThat(
        "YamcsServer is not connected",
        newServer.getServerState(),
        equalTo(ConnectionState.DISCONNECTED));
  }

  @Test
  public void testYamcsServerConnectIncorrect() throws InterruptedException, ExecutionException {
    assertThat("", newConnection.getName(), equalTo("sitl"));
    assertThat("", newConnection.getUrl(), equalTo("localhost"));
    assertThat("", newConnection.getPort(), equalTo(9190));
    assertThat("", newConnection.getUser(), equalTo("admin"));
    assertThat("", newConnection.getPassword(), equalTo("rootpassword"));
    assertThat("", newConnection.toString(), equalTo("localhost:9190"));
    assertThat(
        "YamcsServer is not connected",
        newServer.getServerState(),
        equalTo(ConnectionState.DISCONNECTED));
    newConnection.setUser("not_an_admin");
    assertThat("Attempt to connect fails", newServer.connect(newConnection), equalTo(false));
    assertThat(
        "YamcsServer is not connected",
        newServer.getServerState(),
        equalTo(ConnectionState.DISCONNECTED));
  }

  @BeforeAll
  public static void testYamcsServerTestConnection()
      throws InterruptedException, ExecutionException {
    YamcsServerConnection newConnection =
        new YamcsServerConnection("sitl", "localhost", 9190, "admin", "rootpassword");

    assertThat("", newConnection.getName(), equalTo("sitl"));
    assertThat("", newConnection.getUrl(), equalTo("localhost"));
    assertThat("", newConnection.getPort(), equalTo(9190));
    assertThat("", newConnection.getUser(), equalTo("admin"));
    assertThat("", newConnection.getPassword(), equalTo("rootpassword"));
    assertThat("", newConnection.toString(), equalTo("localhost:9190"));

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
        "yamcs instance is of type \"instance\"",
        newServer.getItems().get(0).getObjectType(),
        equalTo("instance"));

    assertThat(
        "YamcsArchiveClient is not null",
        newServer.getItems().get(0).getYamcsArchiveClient(),
        notNullValue());

    assertThat(
        "eventSubscription is not null",
        newServer.getItems().get(0).getEventSubscription(),
        notNullValue());

    assertThat(
        "yamcsProcessor is not null",
        newServer.getItems().get(0).getYamcsProcessor(),
        notNullValue());
    assertThat("events is not null", newServer.getItems().get(0).getEvents(), notNullValue());
    assertThat(
        "yamcs instance name is equal to YamcsServer item name",
        newServer.getInstance(yamcsInstance).getName(),
        equalTo(newServer.getItems().get(0).getName()));

    assertThat(
        "CMDR_YamcsInstance name matches \"IntegrationTest\"",
        newServer.getInstance(yamcsInstance).getName(),
        equalTo("IntegrationTest"));
    assertThat("Default instance is null", newServer.getDefaultInstance(), nullValue());

    newServer.setDefaultInstance(yamcsInstance);

    assertThat("Default instance is not null", newServer.getDefaultInstance(), notNullValue());

    YamcsAware listener = new YamcsAware() {};
    newServer.addListener(listener);
    assertThat(
        "Instrance has 0 child items",
        newServer.getInstance(yamcsInstance).getItems().size(),
        equalTo(0));

    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> {
              newServer.getInstance(yamcsInstance).createAndAddChild("New_Child");
            },
            "Expected createAndAddChild(String name) to throw, but it didn't");

    assertTrue(thrown.getMessage().equals("CMDR_YamcsInstance does not allow child items"));
  }

  public void testYamcsServerNullInstance() {
    assertThat("Yamcs instance is null", newServer.getInstance(yamcsInstance), nullValue());
  }

  @AfterEach
  @Override
  public void after() throws InterruptedException {
    super.after();
  }
}
