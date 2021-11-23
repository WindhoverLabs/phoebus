package com.windhoverlabs.commander.core;

public class YamcsServer {

	public enum YamcsConnectionStatus {
		Connected, Disconnected;
	};

	private String url;
	private int port;
	private String user;
	private String password;
	private YamcsConnectionStatus status;

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

	public YamcsServer(String newUrl, int newPort, String newUser) {
		url = newUrl;
		port = newPort;
		user = newUser;

	}

	public YamcsServer(String newUrl, int newPort, String newUser, String newPassword) {
		url = newUrl;
		port = newPort;
		user = newUser;
		password = newPassword;
	}

	public String toString() {
		return String.format("%s:%d\n", url, port);
	}
}
