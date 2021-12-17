package com.windhoverlabs.commander.core;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.equalTo;
public class YamcsNodeTest {

	@Test
	public void testActiveInstance() {
		CMDR_YamcsInstance newInstance = new CMDR_YamcsInstance("yamcs-cfs");
//		newInstance.activate();
		
//		assertThat(newInstance.getState(), equalTo(TmTcNodeState.ACTIVE));
		assertThat(newInstance.getName(), equalTo("yamcs-cfs"));
	}
	
	@Test
	public void testInactiveInstance() {
		CMDR_YamcsInstance newInstance = new CMDR_YamcsInstance("yamcs-cfs");
//		newInstance.deactivate();
		
//		assertThat(newInstance.getState(), equalTo(TmTcNodeState.INACTIVE));
		assertThat(newInstance.getName(), equalTo("yamcs-cfs"));
	}

}
