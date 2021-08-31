package org.windhoverlabs.commander.core;

/**
 * @author lgomez
 *
 *An instance is usually a namespace for data coming from some type of Connection.
 */
public interface Instance {
public void activate();
public void deactivate();
public InstanceState getInstanceState();
}
