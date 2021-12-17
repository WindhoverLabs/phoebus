package com.windhoverlabs.commander.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

public class YamcsObjectManagerTest {

	@Before
	public void addInstances() {
		YamcsObjectManager.getRoot().getItems().add(new YamcsServer("sitl"));
		YamcsObjectManager.getRoot().getItems().get(0).getItems().add(new CMDR_YamcsInstance("yamcs-cfs"));
	}

	@After
	public void removeInstances() {
		YamcsObjectManager.getRoot().getItems().remove(0);
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
		assertThat(YamcsObjectManager.getRoot().getItems().get(0).getItems().get(0).getName(), equalTo("yamcs-cfs"));

	}

	@Test
	public void testGetServerFromName() {
		assertThat(YamcsObjectManager.getRoot().getObjectType(), equalTo("root"));
		assertThat(YamcsObjectManager.getServerFromName("sitl").getName(), equalTo("sitl"));
	}

	@Test
	public void testGetInstanceFromName() {
		assertThat(YamcsObjectManager.getRoot().getObjectType(), equalTo("root"));
		assertThat(YamcsObjectManager.getInstanceFromName("sitl", "yamcs-cfs").getName(), equalTo("yamcs-cfs"));
	}
}
