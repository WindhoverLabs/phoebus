package com.windhoverlabs.commander.core;

import org.yamcs.client.ParameterSubscription;
import org.yamcs.protobuf.YamcsInstance;

/**
 * Data model for a yamcs instance.
 * 
 * @author lgomez
 *
 */
public class YamcsNode implements Node {
	private NodeState currentState;
	private String instanceName;
	private YamcsInstance instance;
	public YamcsInstance getInstance() {
		return instance;
	}

	public void setInstance(YamcsInstance instance) {
		instanceName = instance.getName();
		this.instance = instance;
	}

	private String processor;
	private ParameterSubscription yamcsSubscription = null;

	public YamcsNode(String newName) {
		instanceName = newName;
		currentState = NodeState.ACTIVE;
	}

	public YamcsNode() {
		currentState = NodeState.ACTIVE;
	}

	public String getProcessor() {
		return processor;
	}

	public void setProcessor(String processor) {
		this.processor = processor;
	}

	public String getInstanceName() {
		return instanceName;
	}

	@Override
	public void activate() {
		// TODO Implementation
		currentState = NodeState.ACTIVE;
	}

	@Override
	public void deactivate() {
		// TODO Implementation
		currentState = NodeState.INACTIVE;
	}

	@Override
	public NodeState getState() {
		// TODO Auto-generated method stub
		return currentState;
	}

	public ParameterSubscription getYamcsSubscription() {
		return yamcsSubscription;
	}

}