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
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.util.ExecutorUtils;
import org.nightcode.milter.util.Log;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.nightcode.milter.MilterOptions.NETTY_FAIL_STOP_MODE;
import static org.nightcode.milter.MilterOptions.NETTY_RECONNECT_TIMEOUT_MS;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;
import static org.nightcode.milter.util.Properties.getBoolean;
import static org.nightcode.milter.util.Properties.getLong;

/**
 * MilterGatewayManager.
 */
public class MilterGatewayManager<A extends SocketAddress> implements ChannelFutureListener, Closeable {

  public static final int NEW      = 0x00000000;
  public static final int STARTING = 0x00000001;
  public static final int RUNNING  = 0x00000002;
  public static final int CLOSING  = 0x00000004;
  public static final int CLOSED   = 0x00000008;

  private static final boolean FAIL_STOP_MODE       = false;
  private static final long    RECONNECT_TIMEOUT_MS = 1_000;

  private volatile ChannelFuture channelFuture;

  private final boolean failStopMode;
  private final long    reconnectTimeoutNs;

  private final ServerBootstrap serverBootstrap;

  private final ServerFactory<A>         serverFactory;
  private final MilterHandler            milterHandler;
  private final ScheduledExecutorService executor;

  private final AtomicInteger           state      = new AtomicInteger(NEW);
  private final CompletableFuture<Void> bindFuture = new CompletableFuture<>();

  /**
   * @param serverFactory server factory
   * @param milterHandler milter handler
   */
  public MilterGatewayManager(ServerFactory<A> serverFactory, MilterHandler milterHandler) {
    this.serverFactory = serverFactory;
    this.milterHandler = milterHandler;

    executor = new ScheduledThreadPoolExecutor(1, namedThreadFactory("jmilter-" + serverFactory.localAddress() + "-executor"));

    failStopMode = getBoolean(NETTY_FAIL_STOP_MODE, FAIL_STOP_MODE);

    reconnectTimeoutNs = MILLISECONDS.toNanos(getLong(NETTY_RECONNECT_TIMEOUT_MS, RECONNECT_TIMEOUT_MS));

    serverBootstrap = serverFactory.create();
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
        ChannelInitializer<Channel> initializer = new SessionInitializer(() -> new MilterChannelHandler(milterHandler));
        channelFuture = serverBootstrap.childHandler(initializer).bind().sync()
            .addListener((ChannelFutureListener) future -> {
              if (future.cause() == null && state.compareAndSet(STARTING, RUNNING)) {
                bindFuture.complete(null);
              }
            });
        channelFuture.channel().closeFuture().addListener(this);
      } catch (Exception ex) {
        if (failStopMode) {
          Log.warn().log(getClass(), format("unable bind to %s.", serverFactory.localAddress()), ex);
          bindFuture.completeExceptionally(ex);
          return;
        }
        Log.warn().log(getClass(), format("unable bind to %s, will try again after %s ms.", serverFactory.localAddress()
                , NANOSECONDS.toMillis(reconnectTimeoutNs)), ex);
        executor.schedule(this::connect, reconnectTimeoutNs, NANOSECONDS);
      }
    });
    executor.execute(() -> cf.complete(null));
  }

  private boolean isClosing() {
    return state.get() > RUNNING;
  }
}
