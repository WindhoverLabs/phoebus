package com.windhoverlabs.commander.core;

public class YamcsConnection {
	private String url;
	private int port;
	private String user;

	public String getUser() {
		return user;
	}

	public void setUser(String newUser) {
		user = newUser;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public YamcsConnection(String newUrl, int newPort, String newUser) {
		url = newUrl;
		port = newPort;
		user = newUser;
		
	}
	
	public String toString()
	{
		return String.format("%s:%d\n"
				+ "User:%s", url, port, user);
	}
}
