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
import org.nightcode.milter.config.GatewayConfig;
import org.nightcode.milter.util.ExecutorUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;

/**
 *
 */
public class MilterGatewayManager extends AbstractService implements ChannelFutureListener {

  private static final Logger LOGGER = Logger.getLogger(MilterGatewayManager.class.getName());

  private volatile ChannelFuture channelFuture;

  private final GatewayConfig config;

  private final EventLoopGroup acceptorGroup;
  private final EventLoopGroup workerGroup;

  private final ServerBootstrap serverBootstrap;
  private final ServiceManager serviceManager;

  private final Provider<SimpleChannelInboundHandler<MilterPacket>> provider;

  private final ScheduledThreadPoolExecutor scheduledExecutor
      = new ScheduledThreadPoolExecutor(1, namedThreadFactory("MilterGatewayManager.scheduledExecutor"));

  /**
   * @param config gateway config
   * @param provider {@link io.netty.channel.ChannelInboundHandler} provider
   * @param serviceManager ServiceManager instance
   */
  @Inject
  public MilterGatewayManager(GatewayConfig config, Provider<SimpleChannelInboundHandler<MilterPacket>> provider,
      ServiceManager serviceManager) {
    super(MilterGatewayManager.class.getSimpleName());
    this.config = config;
    this.provider = provider;
    this.serviceManager = serviceManager;

    String nettyTransport = System.getProperty("jmilter.netty.transport", "NIO");

    EventLoopGroup tmpAcceptorGroup = null;
    EventLoopGroup tmpWorkerGroup = null;
    Class<? extends ServerChannel> serverChannelClass = null;
    if ("EPOL".equalsIgnoreCase(nettyTransport)) {
      try {
        tmpAcceptorGroup = new EpollEventLoopGroup(1, namedThreadFactory("MilterGatewayManager.nettyEpollAcceptor"));
        tmpWorkerGroup = new EpollEventLoopGroup(0, namedThreadFactory("MilterGatewayManager.nettyEpollWorker"));
        serverChannelClass = EpollServerSocketChannel.class;
      } catch (Throwable ex) {
        LOGGER.log(Level.CONFIG, "can't initialize netty EPOLL transport, switch to NIO");
      }
    } else if ("KQUEUE".equalsIgnoreCase(nettyTransport)) {
      try {
        tmpAcceptorGroup = new KQueueEventLoopGroup(1, namedThreadFactory("MilterGatewayManager.nettyKQueueAcceptor"));
        tmpWorkerGroup = new KQueueEventLoopGroup(0, namedThreadFactory("MilterGatewayManager.nettyKQueueWorker"));
        serverChannelClass = KQueueServerSocketChannel.class;
      } catch (Throwable ex) {
        LOGGER.log(Level.CONFIG, "can't initialize netty KQUEUE transport, switch to NIO");
      }
    }

    if (serverChannelClass == null) {
      tmpAcceptorGroup = new NioEventLoopGroup(1, namedThreadFactory("MilterGatewayManager.nettyNioAcceptor"));
      tmpWorkerGroup = new NioEventLoopGroup(0, namedThreadFactory("MilterGatewayManager.nettyNioWorker"));
      serverChannelClass = NioServerSocketChannel.class;
    }

    acceptorGroup = tmpAcceptorGroup;
    workerGroup = tmpWorkerGroup;

    serverBootstrap = new ServerBootstrap()
        .localAddress(this.config.getAddress(), this.config.getPort())
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
    scheduledExecutor.schedule(this::connect, 1000, TimeUnit.MILLISECONDS);
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
      ExecutorUtils.shutdown(scheduledExecutor);
      acceptorGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
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
        SessionInitializer sessionInitializer = new SessionInitializer(config, provider);
        channelFuture = serverBootstrap.childHandler(sessionInitializer).bind().sync();
        channelFuture.channel().closeFuture().addListener(this);
      } catch (Exception ex) {
        LOGGER.log(Level.WARNING, "can't bind to " + config.getAddress() + ":" + config.getPort()
            + ", will try again after 5 sec.", ex);
        scheduledExecutor.schedule(this::connect, 1000, TimeUnit.MILLISECONDS);
      }
    });
    scheduledExecutor.execute(() -> cf.complete(null));
  }
}
