package com.windhoverlabs.commander.core;

import java.util.List;

/**
 * A connection is a link to the ground system in the outside world. A YAMCS server is example of this.
 * @author lgomez
 *
 */
public abstract class NodeContext<T extends Node> {
protected List<T> nodes = null;
protected List<CommanderPlugin> plugins;
// TODO: Not sure if these functions make sense for this interface.
public abstract void connect();
public abstract void disconnect();
}