package com.windhoverlabs.commander.core;

import org.yamcs.client.YamcsClient;
import org.yamcs.client.processor.ProcessorClient;

import com.windhoverlabs.pv.yamcs.YamcsPV;
import com.windhoverlabs.yamcs.script.Yamcs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CMDR_YamcsInstance extends YamcsObject<YamcsObject<?>> {
	public static String OBJECT_TYPE = "instance";
	private ProcessorClient yamcsProcessor = null;

	public ProcessorClient getYamcsProcessor() {
		return yamcsProcessor;
	}

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
	protected void initProcessorClient(YamcsClient yamcsClient) {
		yamcsProcessor = yamcsClient.createProcessorClient(getName(), "realtime");
	}
}
