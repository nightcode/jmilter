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

package org.nightcode.milter.client;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;
import org.nightcode.milter.Actions;
import org.nightcode.milter.CommandCode;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.net.SessionInitializer;
import org.nightcode.milter.util.Log;

import static java.lang.String.format;
import static org.nightcode.milter.CommandCode.SMFIC_OPTNEG;
import static org.nightcode.milter.client.MilterPacketFactory.createOptneg;

class MilterSessionFactoryImpl<A extends SocketAddress> implements MilterSessionFactory {

  final class OptnegHandler extends ChannelDuplexHandler {
    private static final int PROTOCOL_MIN_VERSION = 2;

    private ChannelHandlerContext ctx;
    private ChannelPromise        connectPromise;
    private ScheduledFuture<?>    timeoutFuture;

    private final CommandCode command = SMFIC_OPTNEG;

    @Override public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                                  ChannelPromise connectPromise) throws Exception {
      this.ctx            = ctx;
      this.connectPromise = connectPromise;

      ChannelPromise promise = ctx.channel().newPromise();
      super.connect(ctx, remoteAddress, localAddress, promise);
      promise.addListener(future -> onConnect(ctx));
    }

    @Override public void channelRead(ChannelHandlerContext ctx, Object msg) {
      if (!(msg instanceof MilterPacket)) {
        onFailure(new IllegalStateException("[" + id(ctx) + "] received unexpected message of " + msg.getClass()));
        return;
      }
      timeoutFuture.cancel(true);
      MilterPacket packet = (MilterPacket) msg;

      if (command.code() != packet.command()) {
        onFailure(new IllegalStateException("[" + id(ctx) + "] received unexpected Milter Command 0x"
            + Integer.toHexString(packet.command()) + " '" + (char) packet.command() + '\''));
        return;
      }

      int payloadLength = packet.payload().length;
      if (payloadLength != 12) {
        onFailure(new IllegalStateException(format("[%s] wrong packet length=%s %s", id(ctx), payloadLength, packet)));
        return;
      }

      int protocolVersion = packet.payload()[3];
      if (protocolVersion < PROTOCOL_MIN_VERSION) {
        onFailure(new IllegalStateException(format("[%s] Milter protocol version too old %s < %s"
            , id(ctx), protocolVersion, PROTOCOL_MIN_VERSION)));
        return;
      }

      Actions       actions       = new Actions(packet.payload(), 4);
      ProtocolSteps protocolSteps = new ProtocolSteps(packet.payload(), 8);

      MilterSession session = new MilterSessionImpl(ctx.channel(), protocolVersion, actions, protocolSteps, channels);
      ctx.channel().attr(SESSION_KEY).set(session);
      Log.debug().log(getClass(), () -> format("[%s] established new milter session", id(ctx)));

      ctx.pipeline().remove(this);
      connectPromise.trySuccess();
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      onFailure(cause);
      ctx.close();
    }

    private void onConnect(ChannelHandlerContext ctx) {
      MilterPacket  packet      = createOptneg(version, actions, steps);
      ChannelFuture writeFuture = ctx.writeAndFlush(packet);

      writeFuture.addListener((ChannelFutureListener) cf -> {
        if (cf.isSuccess()) {
          timeoutFuture = ctx.channel().eventLoop().schedule(this::onTimeout, command.responseTimeoutMs(), TimeUnit.MILLISECONDS);
        } else {
          completeExceptionally(cf.cause());
        }
      });
    }

    private void onFailure(Throwable cause) {
      if (timeoutFuture != null) {
        timeoutFuture.cancel(true);
      }
      completeExceptionally(cause);
    }

    private void onTimeout() {
      Log.warn().log(getClass(), format("[%s] %s packet has not arrived within %s ms", id(ctx), command, command.responseTimeoutMs()));
      completeExceptionally(ReadTimeoutException.INSTANCE);
    }

    private void completeExceptionally(Throwable cause) {
      if (connectPromise.tryFailure(cause)) {
        ctx.channel().close();
      }
    }
  }

  static final AttributeKey<MilterSession> SESSION_KEY = AttributeKey.newInstance("session");

  private static String id(ChannelHandlerContext ctx) {
    return ctx.channel().id().asLongText();
  }

  private final ConnectionFactory<A> factory;
  private final int                  version;
  private final Actions              actions;
  private final ProtocolSteps        steps;

  private final Set<Channel>            channels       = new ConcurrentSkipListSet<>();
  private final AtomicBoolean           shutdown       = new AtomicBoolean(false);
  private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();

  MilterSessionFactoryImpl(MilterSessionFactoryBuilder<A> builder) {
    factory = builder.factory;
    version = builder.protocolVersion;
    actions = builder.actions;
    steps   = builder.protocolSteps;
  }

  @Override public void close() {
    if (!shutdown.compareAndSet(false, true)) {
      return;
    }
    final int todo = channels.size();
    final AtomicInteger done = new AtomicInteger(0);
    for (Channel channel : channels) {
      channel.close().addListener((ChannelFutureListener) future -> {
        if (done.incrementAndGet() == todo) {
          shutdownFuture.complete(null);
        }
      });
    }
  }

  @Override public CompletableFuture<MilterSession> createSession() {
    CompletableFuture<MilterSession> resultFuture = new CompletableFuture<>();

    Bootstrap bootstrap = factory.create();
    bootstrap.handler(new SessionInitializer(createOptnegHandler()));

    ChannelFuture connectFuture = bootstrap.connect(factory.remoteAddress());
    connectFuture.addListener((ChannelFutureListener) future -> connectCallback(future, resultFuture));

    return resultFuture;
  }

  @Override public CompletableFuture<Void> shutdownGracefully() {
    close();
    return shutdownFuture;
  }

  OptnegHandler createOptnegHandler() {
    return new OptnegHandler();
  }

  private void connectCallback(ChannelFuture future, CompletableFuture<MilterSession> resultFuture) {
    if (future.cause() != null) {
      Log.info().log(getClass(), () -> format("connection to %s was not established", factory.remoteAddress()), future.cause());
      resultFuture.completeExceptionally(future.cause());
      return;
    }

    Channel channel = future.channel();
    channels.add(channel);

    MilterSession session = channel.attr(SESSION_KEY).get();

    channel.pipeline().addLast("milterResponseHandler", new ResponseHandler());

    Log.debug().log(getClass(), () -> format("[%s] connection %s established", channel.id().asLongText(), channel));
    resultFuture.complete(session);
  }
}
