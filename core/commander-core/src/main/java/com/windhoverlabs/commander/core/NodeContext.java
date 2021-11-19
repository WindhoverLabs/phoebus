package com.windhoverlabs.commander.core;

import java.util.List;

/**
 * A NodeContext is a link to the ground system in the outside world. A YAMCS
 * server is an example of this.
 * 
 * @author lgomez
 *
 */
public abstract class NodeContext<T extends Node> {
	public enum NodeType {
		YAMCS
	};

	protected List<T> nodes = null;
	private NodeType type;
	public NodeType getType() {
		return type;
	}

	protected List<CommanderPlugin> plugins;

// TODO: Not sure if these functions make sense for this interface.
	public abstract void connect();

	public abstract void disconnect();
	
	protected abstract NodeContext createContext(NodeType t);

}
