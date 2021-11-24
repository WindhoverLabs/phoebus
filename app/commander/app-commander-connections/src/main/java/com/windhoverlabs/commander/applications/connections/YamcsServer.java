package com.windhoverlabs.commander.applications.connections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class YamcsServer extends YamcsObject<YamcsInstance>{
	public static String OBJECT_TYPE = "server";
	
    public YamcsServer(String name) {
        super(name);
    }

    @Override
    public void createAndAddChild(String name) {
        getItems().add(new YamcsInstance(name));
    }
    
    @Override
    public String getObjectType() {
    	return OBJECT_TYPE;
    }
}
