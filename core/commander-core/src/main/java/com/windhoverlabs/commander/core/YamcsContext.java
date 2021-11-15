package com.windhoverlabs.commander.core;

import java.util.ArrayList;
import java.util.HashMap;
import org.yamcs.client.YamcsClient;

/**
 * 
 * @author lgomez
 *
 */
public class YamcsContext extends NodeContext<YamcsNode> {
	private YamcsClient yamcsClient;
	private YamcsConnection connection;

	public YamcsConnection getConnection() {
		return connection;
	}

	private HashMap<YamcsNode, MissionDatabase> instanceDBMap;

	private ArrayList<YamcsNode> nodes;

	public String getUrl() {
		return connection.getUrl();
	}

	public int getPort() {
		return connection.getPort();
	}

	public YamcsContext(String newUrl, int newPort, String newUser) {
		connection = new YamcsConnection(newUrl, newPort, newUser);
		nodes = new ArrayList<YamcsNode>();
	}

	public YamcsContext(YamcsConnection newConnection) {
		connection = newConnection;
		nodes = new ArrayList<YamcsNode>();
	}

	public YamcsContext() {
	}

	@Override
	public void connect() {
		yamcsClient = YamcsClient.newBuilder(connection.getUrl(), connection.getPort()).build();
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
	}

//	@Override
//	public void disconnect() {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void addListener(InvalidationListener listener) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void removeListener(InvalidationListener listener) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void addListener(ChangeListener listener) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void removeListener(ChangeListener listener) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String getValue() {
//		// TODO Auto-generated method stub
//		return connection.toString();
//	}
}
