package org.windhoverlabs.commander.core;

import java.util.List;

/**
 * A connection is a link to the ground system in the outside world. A YAMCS server is example of this.
 * @author lgomez
 *
 */
public abstract class Connection<T extends Instance> {
protected List<T> instances = null;
public abstract void connect();
public abstract void disconnect();
}
