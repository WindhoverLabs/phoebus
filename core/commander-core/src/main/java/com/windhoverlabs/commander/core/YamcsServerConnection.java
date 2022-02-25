package com.windhoverlabs.commander.core;

public class YamcsServerConnection {

  private String url;
  private int port;
  private String user;
  private String password;

  private String name;

  public YamcsServerConnection(YamcsServerConnection newConnection) {
    url = newConnection.getUrl();
    port = newConnection.getPort();
    user = newConnection.getUser();
  }

  public YamcsServerConnection(String newName, String newUrl, int newPort, String newUser) {
    url = newUrl;
    port = newPort;
    user = newUser;
  }

  public YamcsServerConnection(
      String newName, String newUrl, int newPort, String newUser, String newPassword) {
    name = newName;
    url = newUrl;
    port = newPort;
    user = newUser;
    password = newPassword;
  }

  public YamcsServerConnection(String newName, String newUrl, int newPort) {
    url = newUrl;
    port = newPort;
    name = newName;
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

  public String getPassword() {
    return password;
  }

  public String toString() {
    return String.format("%s:%d", url, port);
  }
}
