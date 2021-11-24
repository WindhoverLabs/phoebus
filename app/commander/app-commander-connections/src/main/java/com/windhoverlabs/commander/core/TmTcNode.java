package com.windhoverlabs.commander.core;

/**
 * @author lgomez
 *
 *A node is usually a namespace for data coming from some type of Connection.
 *
 */
public interface TmTcNode {
public void activate();
public void deactivate();
public TmTcNodeState getState();
}