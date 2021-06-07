/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.nightcode.milter.net;

import org.nightcode.common.service.AbstractService;
import org.nightcode.common.service.ServiceManager;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.util.ExecutorUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;

/**
 *
 */
public class MilterGatewayManager extends AbstractService implements ChannelFutureListener {

  private static final long RECONNECT_TIMEOUT_MS = 1_000;

  private volatile ChannelFuture channelFuture;

  private final String address;
  private final long reconnectTimeoutNs;

  private final ServerBootstrap serverBootstrap;
  private final ServiceManager serviceManager;

  private final MilterHandler milterHandler;

  private final ScheduledExecutorService executor
      = new ScheduledThreadPoolExecutor(1, namedThreadFactory("MilterGatewayManager.executor"));

  /**
   * @param address gateway address
   * @param milterHandler milter handler
   * @param serviceManager ServiceManager instance
   */
  public MilterGatewayManager(String address, MilterHandler milterHandler, ServiceManager serviceManager)
          throws UnknownHostException {
    this(MilterGatewayManager.class.getSimpleName(), address, milterHandler, serviceManager);
  }

  /**
   * @param name gateway name
   * @param address gateway address
   * @param milterHandler milter handler
   * @param serviceManager ServiceManager instance
   */
  public MilterGatewayManager(String name, String address, MilterHandler milterHandler, ServiceManager serviceManager)
      throws UnknownHostException {
    super(name);
    this.address = address;
    this.milterHandler = milterHandler;
    this.serviceManager = serviceManager;

    reconnectTimeoutNs = MILLISECONDS.toNanos(Long.getLong("jmilter.netty.reconnectTimeoutMs", RECONNECT_TIMEOUT_MS));

    String host;
    int port;
    int colonIndex;
    if (address.charAt(0) == '[') {
      colonIndex = address.indexOf(58);
      int closeBracketIndex = address.lastIndexOf(93);
      if (colonIndex < 0 || closeBracketIndex < colonIndex) {
        throw new IllegalArgumentException("illegal address: " + address);
      }

      host = address.substring(1, closeBracketIndex);
      if (closeBracketIndex + 1 == address.length() || address.charAt(closeBracketIndex + 1) != ':') {
        throw new IllegalArgumentException("illegal port value in address: " + address);
      }

      port = Integer.parseInt(address.substring(closeBracketIndex + 2));
    } else {
      colonIndex = address.indexOf(58);
      if (colonIndex < 0 || address.indexOf(58, colonIndex + 1) >= 0) {
        throw new IllegalArgumentException("illegal address: " + address);
      }

      host = address.substring(0, colonIndex);
      port = Integer.parseInt(address.substring(colonIndex + 1));
    }
    InetAddress inetAddress = InetAddress.getByName(host);

    String nettyTransport = System.getProperty("jmilter.netty.transport", "NIO");

    EventLoopGroup acceptorGroup = null;
    EventLoopGroup workerGroup = null;
    Class<? extends ServerChannel> serverChannelClass = null;
    if ("EPOL".equalsIgnoreCase(nettyTransport)) {
      try {
        acceptorGroup = new EpollEventLoopGroup(1, namedThreadFactory(serviceName() + ".nettyEpollAcceptor"));
        workerGroup = new EpollEventLoopGroup(0, namedThreadFactory(serviceName() + ".nettyEpollWorker"));
        serverChannelClass = EpollServerSocketChannel.class;
      } catch (Throwable ex) {
        logger.config("unabled to initialize netty EPOLL transport, switch to NIO");
      }
    } else if ("KQUEUE".equalsIgnoreCase(nettyTransport)) {
      try {
        acceptorGroup = new KQueueEventLoopGroup(1, namedThreadFactory(serviceName() + ".nettyKQueueAcceptor"));
        workerGroup = new KQueueEventLoopGroup(0, namedThreadFactory(serviceName() + ".nettyKQueueWorker"));
        serverChannelClass = KQueueServerSocketChannel.class;
      } catch (Throwable ex) {
        logger.config("unabled to initialize netty KQUEUE transport, switch to NIO");
      }
    }

    if (serverChannelClass == null) {
      acceptorGroup = new NioEventLoopGroup(1, namedThreadFactory(serviceName() + ".nettyNioAcceptor"));
      workerGroup = new NioEventLoopGroup(0, namedThreadFactory(serviceName() + ".nettyNioWorker"));
      serverChannelClass = NioServerSocketChannel.class;
    }

    serverBootstrap = new ServerBootstrap()
        .localAddress(inetAddress, port)
        .group(acceptorGroup, workerGroup)
        .channel(serverChannelClass)
        .option(ChannelOption.SO_BACKLOG, 128)
        .option(ChannelOption.SO_REUSEADDR, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_REUSEADDR, true)
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
    ;
  }

  @Override public void operationComplete(ChannelFuture future) {
    future.removeListener(this);
    future.channel().close();
    if (!isClosing()) {
      executor.schedule(this::connect, 1000, MILLISECONDS);
    }
  }

  @Override protected void doStart() {
    try {
      connect();
      serviceManager.addShutdownHook(this);
      started();
    } catch (Exception ex) {
      serviceFailed(ex);
    }
  }

  @Override protected void doStop() {
    try {
      ChannelFuture tmpChannelFuture = channelFuture;
      if (tmpChannelFuture != null) {
        tmpChannelFuture.channel().closeFuture().removeListener(this);
        tmpChannelFuture.channel().close();
      }
      channelFuture = null;
      ExecutorUtils.shutdown(executor);
      serverBootstrap.config().group().shutdownGracefully();
      serverBootstrap.config().childGroup().shutdownGracefully();
      serviceManager.removeShutdownHook(this);
      stopped();
    } catch (Exception ex) {
      serviceFailed(ex);
    }
  }

  private void connect() {
    final CompletableFuture<Void> cf = new CompletableFuture<>();
    cf.thenAccept(v -> {
      try {
        ChannelInitializer<SocketChannel> initializer = new SessionInitializer(milterHandler);
        channelFuture = serverBootstrap.childHandler(initializer).bind().sync();
        channelFuture.channel().closeFuture().addListener(this);
      } catch (Exception ex) {
        logger.warn(ex, "unable to bind to %s, will try again after %s ms."
            , address, NANOSECONDS.toMillis(reconnectTimeoutNs));
        executor.schedule(this::connect, reconnectTimeoutNs, NANOSECONDS);
      }
    });
    executor.execute(() -> cf.complete(null));
  }
}
