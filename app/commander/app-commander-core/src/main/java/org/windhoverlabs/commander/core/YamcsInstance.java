package org.windhoverlabs.commander.core;

import org.yamcs.client.ParameterSubscription;

public class YamcsInstance implements Instance {
	private ParameterSubscription yamcsSubscription = null;
	private InstanceState currentState;
	
	@Override
	public void activate() {
		//TODO Implementation
		currentState = InstanceState.ACTIVE;
	}

	@Override
	public void deactivate() {
		//TODO Implementation
		currentState = InstanceState.ACTIVE;
	}

	@Override
	public InstanceState getInstanceState() {
		// TODO Auto-generated method stub
		return currentState;
	}

}
