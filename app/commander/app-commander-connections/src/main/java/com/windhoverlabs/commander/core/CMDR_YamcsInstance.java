package com.windhoverlabs.commander.core;

import com.windhoverlabs.pv.yamcs.YamcsPV;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CMDR_YamcsInstance extends YamcsObject<YamcsObject<?>> {
	public static String OBJECT_TYPE = "instance";
	
    public CMDR_YamcsInstance(String name) {
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
