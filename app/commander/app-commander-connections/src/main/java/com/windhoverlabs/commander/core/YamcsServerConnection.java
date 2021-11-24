package com.windhoverlabs.commander.core;

public class YamcsServerConnection {

	public enum YamcsConnectionStatus {
		Connected, Disconnected;
	};

	private String url;
	private int port;
	private String user;
	private String password;
	private String name;

	private YamcsConnectionStatus status;

	public YamcsServerConnection(String newUrl, int newPort, String newUser) {
		url = newUrl;
		port = newPort;
		user = newUser;
	}

	public YamcsServerConnection(String newUrl, int newPort, String newUser, String newPassword) {
		url = newUrl;
		port = newPort;
		user = newUser;
		password = newPassword;
	}

	public YamcsConnectionStatus getStatus() {
		return status;
	}

	public void setStatus(YamcsConnectionStatus newStatus) {
		this.status = newStatus;
	}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return String.format("%s:%d\n", url, port);
	}
}
