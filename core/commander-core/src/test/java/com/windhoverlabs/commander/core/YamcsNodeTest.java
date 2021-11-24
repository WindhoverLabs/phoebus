package com.windhoverlabs.commander.core;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.equalTo;
public class YamcsNodeTest {

	@Test
	public void testActiveInstance() {
		OLD_CMDR_YamcsInstance newInstance = new OLD_CMDR_YamcsInstance("yamcs-cfs");
		newInstance.activate();
		
		assertThat(newInstance.getState(), equalTo(TmTcNodeState.ACTIVE));
		assertThat(newInstance.getInstanceName(), equalTo("yamcs-cfs"));
	}
	
	@Test
	public void testInactiveInstance() {
		OLD_CMDR_YamcsInstance newInstance = new OLD_CMDR_YamcsInstance("yamcs-cfs");
		newInstance.deactivate();
		
		assertThat(newInstance.getState(), equalTo(TmTcNodeState.INACTIVE));
		assertThat(newInstance.getInstanceName(), equalTo("yamcs-cfs"));
	}

}
