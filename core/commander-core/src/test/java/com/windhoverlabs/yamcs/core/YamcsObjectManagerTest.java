package com.windhoverlabs.yamcs.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.windhoverlabs.pv.yamcs.YamcsAware;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class YamcsObjectManagerTest {

  @BeforeEach
  @Test
  public void addInstances() {
    YamcsAware listener = new YamcsAware() {};
    YamcsObjectManager.addYamcsListener(listener);
    YamcsObjectManager.getRoot().createAndAddChild("sitl");
    YamcsObjectManager.getRoot()
        .getItems()
        .get(0)
        .getItems()
        .add(new CMDR_YamcsInstance("yamcs-cfs"));
  }

  @AfterEach
  public void removeInstances() {
    if (YamcsObjectManager.getDefaultInstance() != null) {
      YamcsObjectManager.setDefaultInstance("sitl", null);
    }
    YamcsObjectManager.getRoot()
        .getItems()
        .remove(0, YamcsObjectManager.getRoot().getItems().size());
  }

  @Test
  public void testGetRoot() {
    assertThat(YamcsObjectManager.getRoot().getObjectType(), equalTo("root"));
  }

  @Test
  public void testAddServer() {
    assertThat(YamcsObjectManager.getRoot().getObjectType(), equalTo("root"));
    assertThat(YamcsObjectManager.getRoot().getItems().get(0).getName(), equalTo("sitl"));
  }

  @Test
  public void testAddServerInstance() {
    assertThat(YamcsObjectManager.getRoot().getObjectType(), equalTo("root"));
    assertThat(
        YamcsObjectManager.getRoot().getItems().get(0).getItems().get(0).getName(),
        equalTo("yamcs-cfs"));
  }

  @Test
  public void testGetServerFromName() {
    assertThat(YamcsObjectManager.getRoot().getObjectType(), equalTo("root"));
    assertThat(YamcsObjectManager.getServerFromName("sitl").getName(), equalTo("sitl"));
  }

  @Test
  public void testGetInstanceFromName() {
    assertThat(YamcsObjectManager.getRoot().getObjectType(), equalTo("root"));
    assertThat(
        YamcsObjectManager.getInstanceFromName("sitl", "yamcs-cfs").getName(),
        equalTo("yamcs-cfs"));
  }

  @Test
  @Order(1)
  public void testSetDefaultInstance() {
    assertThat("Default instance is null", YamcsObjectManager.getDefaultInstance(), nullValue());
    YamcsObjectManager.setDefaultInstance("sitl", "yamcs-cfs");
    assertThat(
        "Default instance name is \"yamcs-cfs\"",
        YamcsObjectManager.getDefaultInstanceName(),
        equalTo("yamcs-cfs"));

    assertThat(
        "Default server name is \"sitl\"",
        YamcsObjectManager.getDefaultServerName(),
        equalTo("sitl"));

    assertThat(
        "",
        YamcsObjectManager.getDefaultServer(),
        equalTo(YamcsObjectManager.getRoot().getItems().get(0)));
  }

  @Test
  @Order(2)
  public void testSetConnectionObjForServer() {
    YamcsObjectManager.setDefaultInstance("sitl", "yamcs-cfs");
    assertThat(
        "The connection name is \"sitl\" ",
        YamcsObjectManager.getRoot().getItems().get(0).getName(),
        equalTo("sitl"));
    YamcsServerConnection newServer = new YamcsServerConnection("airliner", "localhost", 8095);

    YamcsObjectManager.setConnectionObjForServer(newServer, "sitl", "airliner");

    assertThat(
        "The connection has been updated from \"sitl\" " + "to \"airliner\".",
        YamcsObjectManager.getRoot().getItems().get(0).getName(),
        equalTo("airliner"));
  }
}
