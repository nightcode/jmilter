/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nightcode.milter.net;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

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
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.util.ExecutorUtils;
import org.nightcode.milter.util.Log;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;
import static org.nightcode.milter.util.Properties.getBoolean;
import static org.nightcode.milter.util.Properties.getInt;
import static org.nightcode.milter.util.Properties.getLong;

/**
 * MilterGatewayManager.
 */
public class MilterGatewayManager implements ChannelFutureListener, Closeable {

  public static final int NEW      = 0x00000000;
  public static final int STARTING = 0x00000001;
  public static final int RUNNING  = 0x00000002;
  public static final int CLOSING  = 0x00000004;
  public static final int CLOSED   = 0x00000008;

  private static final long RECONNECT_TIMEOUT_MS = 1_000;

  private volatile ChannelFuture channelFuture;

  private final InetSocketAddress address;

  private final long reconnectTimeoutNs;

  private final ServerBootstrap serverBootstrap;

  private final MilterHandler            milterHandler;
  private final ScheduledExecutorService executor;

  private final AtomicInteger           state      = new AtomicInteger(NEW);
  private final CompletableFuture<Void> bindFuture = new CompletableFuture<>();

  /**
   * @param address gateway address
   * @param milterHandler milter handler
   */
  public MilterGatewayManager(InetSocketAddress address, MilterHandler milterHandler) {
    this(MilterGatewayManager.class.getSimpleName(), address, milterHandler);
  }

  /**
   * @param name gateway name
   * @param address gateway address
   * @param milterHandler milter handler
   */
  public MilterGatewayManager(String name, InetSocketAddress address, MilterHandler milterHandler) {
    this.address = address;
    this.milterHandler = milterHandler;

    executor = new ScheduledThreadPoolExecutor(1, namedThreadFactory(name + "-executor"));

    String prefix = "jmilter.netty.";

    reconnectTimeoutNs = MILLISECONDS.toNanos(getLong(prefix + "reconnectTimeoutMs", RECONNECT_TIMEOUT_MS));

    int nThreads = getInt(prefix + "nThreads", 0);

    EventLoopGroup acceptorGroup = null;
    EventLoopGroup workerGroup   = null;
    Class<? extends ServerChannel> serverChannelClass = null;
    try {
      acceptorGroup = new EpollEventLoopGroup(1, namedThreadFactory(name + "-epoll-acceptor"));
      workerGroup = new EpollEventLoopGroup(nThreads, namedThreadFactory(name + "-epoll-worker"));
      serverChannelClass = EpollServerSocketChannel.class;
    } catch (Throwable ex) {
      Log.info().log(getClass(), "unable to initialize netty EPOLL transport, switch to NIO");
    }
    if (serverChannelClass == null) {
      acceptorGroup = new NioEventLoopGroup(1, namedThreadFactory(name + "-nio-acceptor"));
      workerGroup = new NioEventLoopGroup(nThreads, namedThreadFactory(name + "-nio-worker"));
      serverChannelClass = NioServerSocketChannel.class;
    }

    serverBootstrap = new ServerBootstrap()
        .group(acceptorGroup, workerGroup)
        .channel(serverChannelClass)
        .option(ChannelOption.SO_BACKLOG,   getInt(prefix + "soBacklog", 128))
        .option(ChannelOption.SO_REUSEADDR, getBoolean(prefix + "reuseAddress", true))
        .childOption(ChannelOption.SO_KEEPALIVE, getBoolean(prefix + "keepAlive", true))
        .childOption(ChannelOption.SO_REUSEADDR, getBoolean(prefix + "reuseAddress", true))
        .childOption(ChannelOption.TCP_NODELAY,  getBoolean(prefix + "tcpNoDelay", true))
        .childOption(ChannelOption.ALLOCATOR,    PooledByteBufAllocator.DEFAULT)
        .localAddress(address);
  }

  @Override public void operationComplete(ChannelFuture future) {
    future.removeListener(this);
    future.channel().close();
    if (!isClosing()) {
      executor.schedule(this::connect, 1000, MILLISECONDS);
    }
  }

  public CompletableFuture<Void> bind() {
    if (state.compareAndSet(NEW, STARTING)) {
      connect();
    }
    return bindFuture;
  }

  @Override public void close() {
    if (!state.compareAndSet(RUNNING, CLOSING)) {
      return;
    }

    ChannelFuture tmpChannelFuture = channelFuture;
    if (tmpChannelFuture != null) {
      tmpChannelFuture.channel().closeFuture().removeListener(this);
      tmpChannelFuture.channel().close();
    }
    channelFuture = null;
    ExecutorUtils.shutdown(executor);
    serverBootstrap.config().group().shutdownGracefully();
    serverBootstrap.config().childGroup().shutdownGracefully();

    state.set(CLOSED);
  }

  public int getState() {
    return state.get();
  }

  private void connect() {
    final CompletableFuture<Void> cf = new CompletableFuture<>();
    cf.thenAccept(v -> {
      try {
        ChannelInitializer<SocketChannel> initializer = new SessionInitializer(new MilterChannelHandler(milterHandler));
        channelFuture = serverBootstrap.childHandler(initializer).bind().sync()
            .addListener((ChannelFutureListener) future -> {
              if (future.cause() == null && state.compareAndSet(STARTING, RUNNING)) {
                bindFuture.complete(null);
              }
            });
        channelFuture.channel().closeFuture().addListener(this);
      } catch (Exception ex) {
        Log.warn().log(getClass()
            , format("unable to bind to %s, will try again after %s ms.", address, NANOSECONDS.toMillis(reconnectTimeoutNs)), ex);
        executor.schedule(this::connect, reconnectTimeoutNs, NANOSECONDS);
      }
    });
    executor.execute(() -> cf.complete(null));
  }

  private boolean isClosing() {
    return state.get() > RUNNING;
  }
}
