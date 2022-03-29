package com.windhoverlabs.yamcs.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.handler.codec.http.HttpMethod;
import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yamcs.client.ClientException;
import org.yamcs.client.base.HttpClient;

@Disabled("")
public class HttpClientTest extends AbstractIntegrationTest {

  HttpClient client = new HttpClient();

  @BeforeAll
  public static void initYamcs() throws Exception {
    setupYamcs();
  }

  @BeforeEach
  public void beforeEach() throws ClientException {
    super.before();
  }

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

  @AfterEach
  @Override
  public void after() throws InterruptedException {
    super.after();
  }
}
