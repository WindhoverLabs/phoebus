package com.windhoverlabs.commander.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class YamcsServerTest extends AbstractIntegrationTest {

  @Rule public ExpectedException exceptionGrabber = ExpectedException.none();

  @Test
  public void testYamcsServerConnect() {
    YamcsServer newServer = new YamcsServer("sitl");
    assertThat(newServer.getName(), equalTo("sitl"));

    YamcsServerConnection newConnection =
        new YamcsServerConnection("localhost", 9190, "admin", "rootpassword");

    newServer.connect(newConnection);

    assertThat(newServer.getServerState(), equalTo(ConnectionState.CONNECTED));
  }
}
