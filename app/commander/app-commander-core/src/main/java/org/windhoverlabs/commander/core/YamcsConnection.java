package org.windhoverlabs.commander.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.windhoverlabs.pv.yamcs.MissionDatabase;
import org.yamcs.client.ParameterSubscription;
import org.yamcs.client.YamcsClient;

/**
 * 
 * @author lgomez
 *
 */
public class YamcsConnection extends Connection<YamcsInstance> {
	private String url;
	private int port;
	private YamcsClient yamcsClient;
	private HashMap<YamcsInstance, MissionDatabase> instanceDBMap;
	
	public String getUrl() {
		return url;
	}

	public int getPort() {
		return port;
	}

	public YamcsConnection(String newUrl, int newPort) {
		url = newUrl;
		port = newPort;
		instances = new ArrayList<YamcsInstance>();
	}

	@Override
	public void connect() {
		yamcsClient = YamcsClient.newBuilder(url, port).build();
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
	}
}
