package com.windhoverlabs.commander.applications.connections;

import com.windhoverlabs.commander.core.NodeContext;

public class ContextCellModel<T extends NodeContext> {
	T context;
	String  data; //Could be a node or the entire context.
	
	public ContextCellModel(String newContextData) 
	{
		data = newContextData;
	}
	
	
}
