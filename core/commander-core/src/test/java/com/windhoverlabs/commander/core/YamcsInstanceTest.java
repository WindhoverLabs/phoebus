package com.windhoverlabs.commander.core;

import org.junit.Test;

import com.windhoverlabs.commander.core.NodeState;
import com.windhoverlabs.commander.core.YamcsStream;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;
public class YamcsInstanceTest {

	@Test
	public void testActiveInstance() {
		YamcsStream newInstance = new YamcsStream();
		newInstance.activate();
		
		assertThat(newInstance.getStreamState(), equalTo(NodeState.ACTIVE));
	}

}
