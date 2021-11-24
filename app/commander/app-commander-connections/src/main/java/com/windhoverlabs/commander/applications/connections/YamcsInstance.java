package com.windhoverlabs.commander.applications.connections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class YamcsInstance extends YamcsObject<YamcsObject<?>> {
	public static String OBJECT_TYPE = "instance";
	
    public YamcsInstance(String name) {
        super(name);
    }
    
    @Override
    public ObservableList<YamcsObject<?>> getItems() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public void createAndAddChild(String name) {
        throw new IllegalStateException("Information has no child items");
    }
    
    @Override
    public String getObjectType() {
    	return OBJECT_TYPE;
    }

}
