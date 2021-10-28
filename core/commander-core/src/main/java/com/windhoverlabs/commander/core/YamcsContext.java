package com.windhoverlabs.commander.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.yamcs.client.ParameterSubscription;
import org.yamcs.client.YamcsClient;

/**
 * 
 * @author lgomez
 *
 */
public class YamcsContext extends NodeContext<YamcsNode> {
	private String url;
	private int port;
	private YamcsClient yamcsClient;
	private  List<YamcsConnnection> list;
	private HashMap<YamcsNode, MissionDatabase> instanceDBMap;
	
	
	public String getUrl() {
		return url;
	}

	public int getPort() {
		return port;
	}

	public YamcsContext(String newUrl, int newPort) {
		url = newUrl;
		port = newPort;
		nodes = new ArrayList<YamcsNode>();
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
