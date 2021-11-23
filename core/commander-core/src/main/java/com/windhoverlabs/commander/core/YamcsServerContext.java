package com.windhoverlabs.commander.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.yamcs.client.ClientException;
import org.yamcs.client.ParameterSubscription;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.YamcsInstance;

/**
 * 
 * @author lgomez
 *
 */
public class YamcsServerContext extends NodeContext<CMDR_YamcsInstance> {
	private YamcsClient yamcsClient;
	private YamcsServer connection;
	private ParameterSubscription yamcsParameterSubscription = null;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private NodeType type = NodeType.YAMCS;

	public YamcsServer getConnection() {
		return connection;
	}

	private HashMap<CMDR_YamcsInstance, MissionDatabase> instanceDBMap;

	public void setNodes(ArrayList<CMDR_YamcsInstance> nodes) {
		this.nodes = nodes;
	}

	public List<CMDR_YamcsInstance> getNodes() {
		return nodes;
	}

	public String getUrl() {
		return connection.getUrl();
	}

	public int getPort() {
		return connection.getPort();
	}

	public YamcsServerContext(String newUrl, int newPort, String newUser) {
		connection = new YamcsServer(newUrl, newPort, newUser);
		nodes = new ArrayList<CMDR_YamcsInstance>();
	}

	public YamcsServerContext(YamcsServer newConnection) {
		connection = newConnection;
		nodes = new ArrayList<CMDR_YamcsInstance>();
	}

	public YamcsServerContext() {
		nodes = new ArrayList<CMDR_YamcsInstance>();
	}

	public void addNode(String name) {
		nodes.add(new CMDR_YamcsInstance(name));
	}

	@Override
	public void connect() {
		if (yamcsClient != null) {
			yamcsClient.close();
		}
		yamcsClient = YamcsClient.newBuilder(connection.getUrl(), connection.getPort()).build();

		yamcsClient.listInstances().whenComplete((response, exc) -> {

			if (exc == null) {
				for (YamcsInstance instance : response) {
					CMDR_YamcsInstance newNode = new CMDR_YamcsInstance();
					newNode.setInstance(instance);
					nodes.add(newNode);
				}
			}
		});

		try {
			yamcsClient.connectWebSocket();

		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
	}

	@Override
	protected NodeContext createContext(NodeType t) {
		// TODO Auto-generated method stub
		return null;
	}

	public YamcsClient getYamcsClient() {
		return yamcsClient;
	}

	public ParameterSubscription getYamcsParameterSubscription() {
		if (yamcsClient != null) {
			yamcsParameterSubscription = yamcsClient.createParameterSubscription();
		}
		return yamcsParameterSubscription;
	}
	
	//TODO:Eventually this function will be moved outside of this class.
	/**
	 * 
	 * @param pathToInstance The syntax for this string should be something like "Server_A:yamcs-cfs"
	 * @param allServers
	 * @return
	 */
	public static CMDR_YamcsInstance getInstanceFromPath(String pathToInstance, List<YamcsServerContext> allServers) 
	{
		CMDR_YamcsInstance outInstance = null;
		String serverName = pathToInstance.split(":")[0];
		String instanceName = pathToInstance.split(":")[1]; 
		for(YamcsServerContext server: allServers) 
		{
			if(server.getName().equals(serverName)) 
			{
				for(CMDR_YamcsInstance instance: server.getNodes()) 
				{
					if(instance.getInstanceName().equals(instanceName)) 
					{
						outInstance = instance;
					}
				}
			}
		}
		return outInstance;
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
