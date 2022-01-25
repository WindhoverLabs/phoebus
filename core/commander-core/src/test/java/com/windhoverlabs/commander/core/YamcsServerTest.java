package com.windhoverlabs.commander.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.junit.Test;

public class YamcsServerTest extends AbstractIntegrationTest {

  @Test
  public void testYamcsServerConnect() throws InterruptedException, ExecutionException {
    YamcsServer newServer = new YamcsServer("sitl");
    assertThat(newServer.getName(), equalTo("sitl"));

    YamcsServerConnection newConnection =
        new YamcsServerConnection("localhost", 9190, "admin", "rootpassword");

    newServer.connect(newConnection);

    assertThat(newServer.getServerState(), equalTo(ConnectionState.CONNECTED));

    CompletableFuture<String> future = new CompletableFuture<>();

    ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1);
    executorService.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              // TODO:Not sure if this the best way to do this
              Thread.sleep(100);
              assertThat(newServer.getItems().size(), equalTo(1));
              future.complete("Success");
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        });

    assertThat(future.get(), equalTo("Success"));
  }
}
