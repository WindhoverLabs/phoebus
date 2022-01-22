package com.windhoverlabs.commander.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.netty.handler.codec.http.HttpMethod;
import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.yamcs.client.ClientException;
import org.yamcs.client.base.HttpClient;

public class HttpClientTest extends AbstractIntegrationTest {

  HttpClient client = new HttpClient();

  @Test
  public void testConnectionRefused() throws Exception {
    Throwable t = null;
    try {
      client
          .doAsyncRequest("http://localhost:32323/blaba", HttpMethod.GET, null)
          .get(2, TimeUnit.SECONDS);
    } catch (ExecutionException e) {
      t = e.getCause();
    }

    assertNotNull(t);
    assertTrue(t instanceof ConnectException);
  }

  @Test
  public void test404NotFound() throws Exception {
    Throwable t = null;
    try {
      client
          .doAsyncRequest("http://localhost:9190/blaba", HttpMethod.GET, null)
          .get(2, TimeUnit.SECONDS);
    } catch (ExecutionException e) {
      t = e.getCause();
    }

    assertNotNull(t);
    assertTrue(t instanceof ClientException);
    assertTrue(t.getMessage().contains("404"));
  }

  @Test
  public void testYamcsServer() {
    YamcsServer newServer = new YamcsServer("sitl");
    assertThat(newServer.getName(), equalTo("sitl"));

    YamcsServerConnection newConnection = new YamcsServerConnection("localhost", 9190);

    newServer.connect(newConnection);
  }
}
