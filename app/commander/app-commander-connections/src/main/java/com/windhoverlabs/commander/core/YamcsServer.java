package com.windhoverlabs.commander.core;

import org.yamcs.client.ClientException;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.YamcsInstance;

import com.windhoverlabs.commander.core.YamcsServerConnection;
import com.windhoverlabs.pv.yamcs.YamcsPV;
import com.windhoverlabs.pv.yamcs.YamcsSubscriptionService;

public class YamcsServer extends YamcsObject<CMDR_YamcsInstance> {
	public static String OBJECT_TYPE = "server";
	private YamcsClient yamcsClient;
	private YamcsServerConnection connection;

	private boolean isConnected;

	public boolean isConnected() {
		return isConnected;
	}

	private YamcsSubscriptionService paramSubscriptionService;

	public YamcsServer(String name) {
		super(name);
	}

	@Override
	public void createAndAddChild(String name) {
		getItems().add(new CMDR_YamcsInstance(name));
	}

	@Override
	public String getObjectType() {
		return OBJECT_TYPE;
	}

	public void connect(YamcsServerConnection newConnection) {
		connection = newConnection;
		if (yamcsClient != null) {
			yamcsClient.close();
		}
		yamcsClient = YamcsClient.newBuilder(connection.getUrl(), connection.getPort()).build();

		yamcsClient.listInstances().whenComplete((response, exc) -> {

			if (exc == null) {
				for (YamcsInstance instance : response) {
					createAndAddChild(instance.getName());
				}
			}
		});
		
		paramSubscriptionService = new YamcsSubscriptionService(yamcsClient.createParameterSubscription(), this.getName());

		try {
			yamcsClient.connectWebSocket();

		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		isConnected = true;
	}

	public void subscribePV(YamcsPV pv, String instanceName) {
		paramSubscriptionService.register(pv, instanceName);
	}
	
	public YamcsServerConnection getConnection() {
		return connection;
	}
}
