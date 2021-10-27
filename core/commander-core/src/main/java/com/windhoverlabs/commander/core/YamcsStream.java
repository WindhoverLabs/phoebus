package com.windhoverlabs.commander.core;

import org.yamcs.client.ParameterSubscription;

/**
 * Data model for a yamcs instance.
 * @author lgomez
 *
 */
public class YamcsStream implements Node {
	private NodeState currentState;
	private String instanceName;
	private ParameterSubscription yamcsSubscription = null;
	
	@Override
	public void activate() {
		//TODO Implementation
		currentState = NodeState.ACTIVE;
	}

	@Override
	public void deactivate() {
		//TODO Implementation
		currentState = NodeState.ACTIVE;
	}

	@Override
	public NodeState getStreamState() {
		// TODO Auto-generated method stub
		return currentState;
	}

	public ParameterSubscription getYamcsSubscription() {
		return yamcsSubscription;
	}

}