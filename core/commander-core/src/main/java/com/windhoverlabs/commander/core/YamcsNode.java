package com.windhoverlabs.commander.core;

import org.yamcs.client.ParameterSubscription;

/**
 * Data model for a yamcs instance.
 * @author lgomez
 *
 */
public class YamcsNode implements Node {
	private NodeState currentState;
	private String instanceName;
	public String getInstanceName() {
		return instanceName;
	}

	private ParameterSubscription yamcsSubscription = null;
	
	public YamcsNode(String newName) 
	{
		instanceName = newName;
	}
	
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