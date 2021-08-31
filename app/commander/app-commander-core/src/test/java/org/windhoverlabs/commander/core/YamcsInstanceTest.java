package org.windhoverlabs.commander.core;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;
public class YamcsInstanceTest {

	@Test
	public void testActiveInstance() {
		YamcsInstance newInstance = new YamcsInstance();
		newInstance.activate();
		
		assertThat(newInstance.getInstanceState(), equalTo(InstanceState.ACTIVE));
	}

}
