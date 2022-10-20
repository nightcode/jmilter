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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import org.nightcode.milter.AbstractMilterHandler;
import org.nightcode.milter.Actions;
import org.nightcode.milter.CommandCode;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.net.MilterChannelHandler;
import org.nightcode.milter.net.SessionInitializer;
import org.nightcode.milter.util.Log;

import org.junit.Assert;
import org.junit.Test;

import static org.nightcode.milter.CommandCode.SMFIC_HELO;
import static org.nightcode.milter.ResponseCode.SMFIR_CONTINUE;
import static org.nightcode.milter.client.MilterPacketFactory.createEoh;
import static org.nightcode.milter.client.MilterPacketFactory.createHelo;
import static org.nightcode.milter.client.MilterPacketFactory.createMacro;
import static org.nightcode.milter.util.MilterPackets.SMFIS_CONTINUE;

public class ResponseHandlerTest {

  private static final LocalAddress TEST_ADDRESS = new LocalAddress("test.id");

  @Test public void test() throws Exception {
    Bootstrap       clientBootstrap = new Bootstrap();
    ServerBootstrap serverBootstrap = new ServerBootstrap();

    final CompletableFuture<MilterPacket> cf = new CompletableFuture<>();

    try {
      MilterHandler milterHandler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
        @Override public void helo(MilterContext context, String helo) throws MilterException {
          context.setSessionProtocolSteps(ProtocolSteps.DEF_PROTOCOL_STEPS);
          super.helo(context, helo);
        }

        @Override public void quit(MilterContext context) {
          // do nothing
        }
      };

      serverBootstrap.group(new NioEventLoopGroup(2))
          .channel(LocalServerChannel.class)
          .childHandler(new SessionInitializer(() -> new MilterChannelHandler(milterHandler)));

      clientBootstrap.group(new NioEventLoopGroup(1))
          .channel(LocalChannel.class)
          .handler(new SessionInitializer(ResponseHandler::new));

      serverBootstrap.bind(TEST_ADDRESS).sync();

      ChannelFuture channelFuture = clientBootstrap.connect(TEST_ADDRESS);
      Assert.assertTrue(channelFuture.awaitUninterruptibly().isSuccess());

      MilterMessage message = new MilterMessage(SMFIC_HELO
          , new MilterPacket[] {
              createMacro(SMFIC_HELO, Macros.builder().add("key", "value").build()),
              createHelo("mail.example.org")
          }
          , new MilterCallback() {
              @Override public boolean isFinalAction(MilterPacket packet) {
                return true;
              }

              @Override public void onAction(MilterPacket packet) {
                cf.complete(packet);
              }

              @Override public void onFailure(Throwable cause) {
                cf.completeExceptionally(cause);
              }
          }
      );

      Channel clientChannel = channelFuture.channel();
      clientChannel.writeAndFlush(message);

      Assert.assertEquals(SMFIR_CONTINUE.code(), cf.get().command());
    } finally {
      serverBootstrap.config().group().shutdownGracefully();
      serverBootstrap.config().childGroup().shutdownGracefully();
      clientBootstrap.config().group().shutdownGracefully();
    }
  }

  @Test public void testExceptionCaught() {
    ResponseHandler handler = new ResponseHandler();
    EmbeddedChannel channel = new EmbeddedChannel(handler);

    AtomicReference<Throwable> causeReference = new AtomicReference<>();
    
    MilterCallback callback = new MilterCallback() {
      @Override public boolean isFinalAction(MilterPacket packet) {
        return true;
      }

      @Override public void onAction(MilterPacket packet) {
        // do nothing
      }

      @Override public void onFailure(Throwable cause) {
        causeReference.set(cause);
      }
    };

    MilterMessage message = new MilterMessage(CommandCode.SMFIC_EOH, new MilterPacket[] {createEoh()}, callback);
    channel.writeOutbound(message);

    IOException ioEx = new IOException("IO exception");
    channel.pipeline().fireExceptionCaught(ioEx);

    Assert.assertSame(ioEx, causeReference.get());
  }

  @Test public void testNoCallback() {
    ResponseHandler handler = new ResponseHandler();
    EmbeddedChannel channel = new EmbeddedChannel(handler);

    AtomicReference<String> logReference = new AtomicReference<>();
    Log.LoggingHandler noopHandler = (clazz, supplier, thrown) -> {};
    Log.LoggingHandler loggingHandler = (clazz, supplier, thrown) -> logReference.set(supplier.get());
    
    Log.setLoggingHandler(noopHandler, noopHandler, loggingHandler, noopHandler, noopHandler);
    
    MilterMessage message = new MilterMessage(CommandCode.SMFIC_EOH, new MilterPacket[] {createEoh()}, null);
    channel.writeOutbound(message);
    channel.writeInbound(SMFIS_CONTINUE);

    Assert.assertTrue(Pattern.compile("\\[.*] received packet .* but no callback has been registered")
        .matcher(logReference.get()).find());
  }

  @Test public void testChannelInactive() {
    ResponseHandler handler = new ResponseHandler();
    EmbeddedChannel channel = new EmbeddedChannel(handler);

    AtomicReference<Throwable> causeReference = new AtomicReference<>();

    MilterCallback callback = new MilterCallback() {
      @Override public boolean isFinalAction(MilterPacket packet) {
        return true;
      }

      @Override public void onAction(MilterPacket packet) {
        // do nothing
      }

      @Override public void onFailure(Throwable cause) {
        causeReference.set(cause);
      }
    };

    MilterMessage message = new MilterMessage(CommandCode.SMFIC_EOH, new MilterPacket[] {createEoh()}, callback);
    channel.writeOutbound(message);

    channel.pipeline().fireChannelInactive();

    Assert.assertTrue(Pattern.compile("\\[.*] connection to Milter has been closed")
        .matcher(causeReference.get().getMessage()).find()); 
  }
}
