package org.windhoverlabs.commander.core;

/**
 * @author lgomez
 *
 *A stream is usually a namespace for data coming from some type of Connection.
 *@apiNote Not sure if the Stream should just be a Node.
 */
public interface Node {
public void activate();
public void deactivate();
public NodeState getStreamState();
}