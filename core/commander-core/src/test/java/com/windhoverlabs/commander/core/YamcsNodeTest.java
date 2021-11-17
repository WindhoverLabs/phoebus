package com.windhoverlabs.commander.core;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.equalTo;
public class YamcsNodeTest {

	@Test
	public void testActiveInstance() {
		YamcsNode newInstance = new YamcsNode("yamcs-cfs");
		newInstance.activate();
		
		assertThat(newInstance.getState(), equalTo(NodeState.ACTIVE));
		assertThat(newInstance.getInstanceName(), equalTo("yamcs-cfs"));
	}
	
	@Test
	public void testInactiveInstance() {
		YamcsNode newInstance = new YamcsNode("yamcs-cfs");
		newInstance.deactivate();
		
		assertThat(newInstance.getState(), equalTo(NodeState.INACTIVE));
		assertThat(newInstance.getInstanceName(), equalTo("yamcs-cfs"));
	}

}
