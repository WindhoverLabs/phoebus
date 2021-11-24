package com.windhoverlabs.commander.core;

import org.yamcs.client.ParameterSubscription;
import org.yamcs.protobuf.YamcsInstance;

/**
 * Data model for a yamcs instance.
 * 
 * @author lgomez
 *
 */
public class OLD_CMDR_YamcsInstance implements TmTcNode {
	private TmTcNodeState currentState;
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

	public OLD_CMDR_YamcsInstance(String newName) {
		instanceName = newName;
		currentState = TmTcNodeState.ACTIVE;
	}

	public OLD_CMDR_YamcsInstance() {
		currentState = TmTcNodeState.ACTIVE;
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
		currentState = TmTcNodeState.ACTIVE;
	}

	@Override
	public void deactivate() {
		currentState = TmTcNodeState.INACTIVE;
	}

	@Override
	public TmTcNodeState getState() {
		return currentState;
	}
	

	public ParameterSubscription getYamcsSubscription() {
		return yamcsSubscription;
	}
	
	public String getPv(String pvName) 
	{
		return "";
	}

}