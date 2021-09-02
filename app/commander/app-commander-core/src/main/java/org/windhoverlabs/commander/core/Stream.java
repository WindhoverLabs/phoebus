package org.windhoverlabs.commander.core;

/**
 * @author lgomez
 *
 *A stream is usually a namespace for data coming from some type of Connection.
 */
public interface Stream {
public void activate();
public void deactivate();
public StreamState getStreamState();
}
