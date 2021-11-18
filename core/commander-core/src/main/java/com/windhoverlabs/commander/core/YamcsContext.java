package com.windhoverlabs.commander.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.yamcs.client.ClientException;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.YamcsInstance;

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

	public void setNodes(ArrayList<YamcsNode> nodes) {
		this.nodes = nodes;
	}

	public List<YamcsNode> getNodes() {
		return nodes;
	}

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

	public void addNode(String name) {
		nodes.add(new YamcsNode(name));
	}

	@Override
	public void connect() {
		if (yamcsClient != null) {
			yamcsClient.close();
		}
		System.out.println("yamcs connect1");
		yamcsClient = YamcsClient.newBuilder(connection.getUrl(), connection.getPort()).build();

		yamcsClient.listInstances().whenComplete((response, exc) -> {

			if (exc == null) {
				List<ProcessorInfo> processors = new ArrayList<>();

				for (YamcsInstance instance : response) {
					YamcsNode newNode = new YamcsNode();
					newNode.setInstance(instance);
					nodes.add(newNode);
					System.out.println("instance name:" + instance);
				}
			}
		});

		try {
			System.out.println("yamcs connect3");

			yamcsClient.connectWebSocket();
			System.out.println("yamcs connect4");

		} catch (ClientException e) {
			// TODO Auto-generated catch block
			System.out.println("yamcs connect5");
			e.printStackTrace();
		}
		System.out.println("yamcs connect6");

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
//YamcsNode
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
