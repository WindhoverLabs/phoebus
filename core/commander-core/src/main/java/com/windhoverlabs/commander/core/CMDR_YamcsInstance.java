package com.windhoverlabs.commander.core;

import org.yamcs.client.YamcsClient;
import org.yamcs.client.processor.ProcessorClient;

import com.windhoverlabs.pv.yamcs.YamcsPV;
import com.windhoverlabs.pv.yamcs.YamcsSubscriptionService;
import com.windhoverlabs.yamcs.script.Yamcs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CMDR_YamcsInstance extends YamcsObject<YamcsObject<?>> {
	public static String OBJECT_TYPE = "instance";
	private ProcessorClient yamcsProcessor = null;
	private YamcsSubscriptionService paramSubscriptionService;

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

	protected void initYamcsSubscriptionService(YamcsClient yamcsClient, String serverName) {
		paramSubscriptionService = new YamcsSubscriptionService(yamcsClient.createParameterSubscription(), serverName,
				this.getName());
	}

	public void subscribePV(YamcsPV pv) {
		// TODO:Have to let the caller know whether were able to successfully subscribe
		// to this pv or not.
		paramSubscriptionService.register(pv);
	}
}
