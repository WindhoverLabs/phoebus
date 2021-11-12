package com.windhoverlabs.commander.core;

public class YamcsConnection {
	private String url;
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

	private int port;
	
	public YamcsConnection(String newUrl, int newPort) {
		url = newUrl;
		port = newPort;
	}
}
