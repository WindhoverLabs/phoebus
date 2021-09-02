package org.windhoverlabs.commander.core;

import org.yamcs.client.ParameterSubscription;

/**
 * Data model for a yamcs instance.
 * @author lgomez
 *
 */
public class YamcsStream implements Stream {
	private StreamState currentState;
	private String instanceName;
	private ParameterSubscription yamcsSubscription = null;
	
	@Override
	public void activate() {
		//TODO Implementation
		currentState = StreamState.ACTIVE;
	}

	@Override
	public void deactivate() {
		//TODO Implementation
		currentState = StreamState.ACTIVE;
	}

	@Override
	public StreamState getStreamState() {
		// TODO Auto-generated method stub
		return currentState;
	}

	public ParameterSubscription getYamcsSubscription() {
		return yamcsSubscription;
	}

}