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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.unix.DomainSocketAddress;
import org.nightcode.milter.AbstractMilterHandler;
import org.nightcode.milter.Actions;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.client.ConnectionFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.nightcode.milter.CommandCode.SMFIC_OPTNEG;

public class UnixSocketTest {

  private static final DomainSocketAddress TEST_ADDRESS = new DomainSocketAddress("target/jmilter-test.sock");

  @Before public void setUp() {
    Assume.assumeTrue(Epoll.isAvailable() || KQueue.isAvailable());
  }

  @After public void tearDown() throws Exception {
    Files.deleteIfExists(Paths.get(TEST_ADDRESS.path()));
  }

  @Test public void testUnixSocketConnectionFactory() throws Exception {
    final CountDownLatch negotiateLatch = new CountDownLatch(1);

    MilterHandler milterHandler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void optneg(MilterContext context, int mtaProtocolVersion, Actions mtaActions,
                                   ProtocolSteps mtaProtocolSteps) throws MilterException {
        super.optneg(context, mtaProtocolVersion, mtaActions, mtaProtocolSteps);
        negotiateLatch.countDown();
      }

      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    ConnectionFactory<DomainSocketAddress> clientFactory = ConnectionFactory.unixSocketFactory(TEST_ADDRESS);
    ServerFactory<DomainSocketAddress>     serverFactory = ServerFactory.unixSocketFactory(TEST_ADDRESS);

    Bootstrap clientBootstrap = clientFactory.create();

    try (MilterGatewayManager<DomainSocketAddress> gatewayManager = new MilterGatewayManager<>(serverFactory, milterHandler)) {
      gatewayManager.bind().get(500, SECONDS);

      clientBootstrap
          .handler(new SessionInitializer(() -> new SimpleChannelInboundHandler<ByteBuf>() {
            @Override protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
              // do nothing
            }
          }));

      ChannelFuture channelFuture = clientBootstrap.connect(TEST_ADDRESS);
      Assert.assertTrue(channelFuture.awaitUninterruptibly().isSuccess());

      Channel clientChannel = channelFuture.channel();
      clientChannel.writeAndFlush(Unpooled.copiedBuffer(new byte[]{(byte) SMFIC_OPTNEG.code()
          , 0x00, 0x00, 0x00, 0x06
          , 0x00, 0x00, 0x01, (byte) 0xFF
          , 0x00, 0x1F, (byte) 0xFF, (byte) 0xFF
      }));

      Assert.assertTrue(negotiateLatch.await(5, SECONDS));
    } finally {
      clientBootstrap.config().group().shutdownGracefully();
    }
  }
}