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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

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
public class WebSocketClient {

  static final String URL = System.getProperty("url", "ws://127.0.0.1:8090/websocket");

  public WebSocketClient() {
    URI uri = null;
    try {
      uri = new URI(URL);
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
    final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
    final int port;
    if (uri.getPort() == -1) {
      if ("ws".equalsIgnoreCase(scheme)) {
        port = 80;
      } else if ("wss".equalsIgnoreCase(scheme)) {
        port = 443;
      } else {
        port = -1;
      }
    } else {
      port = uri.getPort();
    }

    if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
      System.err.println("Only WS(S) is supported.");
      return;
    }

    SslContext sslCtx = null;

    EventLoopGroup group = new NioEventLoopGroup();
    try {
      // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
      // If you change it to V00, ping is not supported and remember to change
      // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
      final WebSocketClientHandler handler =
          new WebSocketClientHandler(
              WebSocketClientHandshakerFactory.newHandshaker(
                  uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .handler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                  ChannelPipeline p = ch.pipeline();
                  if (sslCtx != null) {
                    p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                  }
                  p.addLast(
                      new HttpClientCodec(),
                      new HttpObjectAggregator(8192),
                      WebSocketClientCompressionHandler.INSTANCE,
                      handler);
                }
              });

      Channel ch = null;
      try {
        ch = b.connect(uri.getHost(), port).sync().channel();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      try {
        handler.handshakeFuture().sync();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
      while (true) {
        String msg = null;
        try {
          msg = console.readLine();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        if (msg == null) {
          break;
        } else if ("bye".equals(msg.toLowerCase())) {
          ch.writeAndFlush(new CloseWebSocketFrame());
          try {
            ch.closeFuture().sync();
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          break;
        } else if ("ping".equals(msg.toLowerCase())) {
          WebSocketFrame frame =
              new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] {8, 1, 8, 1}));
          ch.writeAndFlush(frame);
        } else {
          WebSocketFrame frame = new TextWebSocketFrame(msg);
          ch.writeAndFlush(frame);
        }
      }
    } finally {
      group.shutdownGracefully();
    }
  }
}
