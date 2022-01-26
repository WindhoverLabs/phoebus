package com.windhoverlabs.commander.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.junit.Test;
import org.yamcs.client.ClientException;

public class YamcsServerTest extends AbstractIntegrationTest {
  private YamcsServer newServer;

  @Override
  public void before() throws ClientException {
    super.before();
    newServer = new YamcsServer("sitl");
    assertThat(newServer.getName(), equalTo("sitl"));

    YamcsServerConnection newConnection =
        new YamcsServerConnection("localhost", 9190, "admin", "rootpassword");

    newServer.connect(newConnection);
  }

  @Test
  public void testYamcsServerConnect() throws InterruptedException, ExecutionException {
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
}
