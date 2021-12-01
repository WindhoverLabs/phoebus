package com.windhoverlabs.commander.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;

import org.junit.Test;

public class YamcsServerContextTest {

	
	@Test
	public void testPVPathToYamcsInstance() {
		YamcsServerContext newServerA = new YamcsServerContext();
		newServerA.setName("Server_A");
		newServerA.addNode("yamcs-cfs-A");

		YamcsServerContext newServerB = new YamcsServerContext();
		newServerB.setName("Server_B");
		newServerB.addNode("yamcs-cfs-B");

		String pathToInstance = "Server_A:yamcs-cfs-A";

		ArrayList<YamcsServerContext> allServers = new ArrayList<YamcsServerContext>();

		allServers.add(newServerB);
		allServers.add(newServerA);

		OLD_CMDR_YamcsInstance instanceResult = YamcsServerContext.getInstanceFromPath(pathToInstance, allServers);

		assertThat(newServerA.getNodes().get(0), equalTo(instanceResult));
		assertThat(newServerA.getNodes().get(0).getInstanceName(), equalTo(instanceResult.getInstanceName()));

		pathToInstance = "Server_B:yamcs-cfs-B";

		instanceResult = YamcsServerContext.getInstanceFromPath(pathToInstance, allServers);

		assertThat(newServerB.getNodes().get(0), equalTo(instanceResult));
		assertThat(newServerB.getNodes().get(0).getInstanceName(), equalTo(instanceResult.getInstanceName()));

	}
}
