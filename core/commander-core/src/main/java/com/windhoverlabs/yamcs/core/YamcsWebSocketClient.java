/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.windhoverlabs.yamcs.core;

import com.google.gson.Gson;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.function.Consumer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * This is an example of a WebSocket client.
 *
 * <p>In order to run this example you need a compatible WebSocket server. Therefore you can either
 * start the WebSocket server from the examples by running {@link
 * io.netty.example.http.websocketx.server.WebSocketServer} or connect to an existing WebSocket
 * server such as <a href="https://www.websocket.org/echo.html">ws://echo.websocket.org</a>.
 *
 * <p>The client will attempt to connect to the URI passed to it as the first argument. You don't
 * have to specify any arguments if you want to connect to the example WebSocket server, as this is
 * the default.
 */
public class YamcsWebSocketClient {

  class YamcsMessage {
    public String type;
    public SubscribeTMStatisticsRequest options;

    public YamcsMessage(String type, SubscribeTMStatisticsRequest options) {
      this.type = type;
      this.options = options;
    }
  }

  class SubscribeTMStatisticsRequest {
    String instance;
    String processor;

    public SubscribeTMStatisticsRequest(String instance, String processor) {
      this.instance = instance;
      this.processor = processor;
    }
  }

  public class TmStatistics {

    // Packet name.
    public String packetName;
    public String qualifiedName;
    public String receivedPackets; // String decimal
    public String subscribedParameterCount;
    public String lastReceived; // RFC 3339 timestamp
    public String lastPacketTime; // RFC 3339 timestamp
    public String packetRate; // String decimal
    public String dataRate; // String decimal
  }

  class Statistics {
    // Yamcs instance name.
    String instance;

    // Processor name.
    String processor;
    ArrayList<TmStatistics> tmstats;
    String lastUpdated; // RFC 3339 timestamp
  }

  class StatisticsMessage {
    String type;
    int call;
    int seq;
    Statistics data;
  }

  private WebSocketClient mWs;
  private Consumer<ArrayList<TmStatistics>> statsConsumer;
  private String instance;
  private String processor;

  public YamcsWebSocketClient(
      Consumer<ArrayList<TmStatistics>> consumer,
      String address,
      int port,
      String instance,
      String processor) {
    this.statsConsumer = consumer;
    this.instance = instance;
    this.processor = processor;
    try {
      mWs =
          new WebSocketClient(
              new URI("ws://" + address + ":" + Integer.toString(port) + "/api/websocket")) {
            @Override
            public void onMessage(String message) {
              Gson obj = new Gson();
              StatisticsMessage stats = obj.fromJson(message, StatisticsMessage.class);
              statsConsumer.accept(stats.data.tmstats);
            }

            @Override
            public void onOpen(ServerHandshake handshake) {
              System.out.println("opened connection");
              Gson obj = new Gson();
              subscribeTMStatistics(instance, processor, obj);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
              System.out.println("closed connection:" + reason);
              System.out.println("closed connection:" + code);
              System.out.println("closed connection:" + remote);
              //              mWs.reconnect();

            }

            @Override
            public void onError(Exception ex) {
              ex.printStackTrace();
            }
          };

      //     Disable timeout
      mWs.setConnectionLostTimeout(0);

      mWs.connect();
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // open websocket
  }

  private void subscribeTMStatistics(String instance, String processor, Gson obj) {
    //  		TODO:Create a map that maps the instance to the specific call id. This way we don't open a
    // connection for each instance...
    //  		This would be irrelevant if there was a stats subscription in the YamcsClient Java API.
    String message =
        obj.toJson(
            new YamcsMessage("tmstats", new SubscribeTMStatisticsRequest(instance, processor)));
    // send message
    mWs.send(message);
  }

  public void close() {
    mWs.close();
  }
}
