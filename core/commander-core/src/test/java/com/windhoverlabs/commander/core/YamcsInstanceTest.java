package com.windhoverlabs.commander.core;

import org.junit.Test;

import com.windhoverlabs.commander.core.NodeState;
import com.windhoverlabs.commander.core.YamcsNode;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;
public class YamcsInstanceTest {

	@Test
	public void testActiveInstance() {
		YamcsNode newInstance = new YamcsNode("");
		newInstance.activate();
		
		assertThat(newInstance.getStreamState(), equalTo(NodeState.ACTIVE));
	}

}
