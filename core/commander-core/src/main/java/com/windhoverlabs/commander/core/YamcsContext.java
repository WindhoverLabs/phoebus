package com.windhoverlabs.commander.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.yamcs.client.ParameterSubscription;
import org.yamcs.client.YamcsClient;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * 
 * @author lgomez
 *
 */
public class YamcsContext extends NodeContext<YamcsNode>  {
	private YamcsClient yamcsClient;
	private  YamcsConnection connection;
	private HashMap<YamcsNode, MissionDatabase> instanceDBMap;
	String User;
	private ArrayList<YamcsNode> nodes;
	
	public String getUrl() {
		return connection.getUrl();
	}

	public int getPort() {
		return connection.getPort();
	}

	public YamcsContext(String newUrl, int newPort, String newUser) {
		connection = new YamcsConnection(newUrl, newPort);
		nodes = new ArrayList<YamcsNode>();
		User = newUser;
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
