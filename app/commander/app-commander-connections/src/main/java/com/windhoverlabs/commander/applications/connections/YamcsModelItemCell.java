package com.windhoverlabs.commander.applications.connections;

import com.windhoverlabs.commander.core.YamcsServerContext;

public class YamcsModelItemCell {	
	enum YamcsItemTreeCellType {
		Server, Instance
	};
	
	private YamcsServerContext contextModel;
	
	private boolean isRoot;
	private String data;
	private YamcsItemTreeCellType type;

	public YamcsModelItemCell(YamcsServerContext newContextModel, boolean newIsRoot, String newData) {
		contextModel = newContextModel;
		isRoot = newIsRoot;
		data = newData;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public String getData() {
		if (isRoot) {
			return "Yamcs";
		}
		return data;
	}
}
