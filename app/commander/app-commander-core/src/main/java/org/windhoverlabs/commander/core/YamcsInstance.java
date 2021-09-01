package org.windhoverlabs.commander.core;

import org.yamcs.client.ParameterSubscription;

/**
 * Data model for a yamcs instance.
 * @author lgomez
 *
 */
public class YamcsInstance implements Instance {
	private InstanceState currentState;
	private String instanceName;
	private ParameterSubscription yamcsSubscription = null;
	
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

	public ParameterSubscription getYamcsSubscription() {
		return yamcsSubscription;
	}

}