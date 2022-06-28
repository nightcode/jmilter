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

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NetUtil;
import org.nightcode.milter.AbstractMilterHandler;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.codec.Int32LenFrameEncoder;
import org.nightcode.milter.codec.MilterPacketEncoder;
import org.nightcode.milter.Actions;
import org.nightcode.milter.ProtocolSteps;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.nightcode.milter.CommandCode.SMFIC_OPTNEG;

public class MilterChannelHandlerTest {

  private static final LocalAddress TEST_ADDRESS = new LocalAddress("test.id");

  private static EventLoopGroup sharedGroup;

  @BeforeClass public static void beforeClass() {
    sharedGroup = new DefaultEventLoopGroup(1);
  }

  @AfterClass public static void afterClass() throws InterruptedException {
    sharedGroup.shutdownGracefully(0, 0, SECONDS).await();
  }

  @Test public void test() throws InterruptedException {
    Bootstrap clientBootstrap = new Bootstrap();
    ServerBootstrap serverBootstrap = new ServerBootstrap();

    final CountDownLatch negotiateLatch = new CountDownLatch(1);

    try {
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

      serverBootstrap.group(new NioEventLoopGroup(2))
          .channel(NioServerSocketChannel.class)
          .childHandler(new SessionInitializer(new MilterChannelHandler(milterHandler)));

      clientBootstrap.group(new NioEventLoopGroup(1))
          .channel(NioSocketChannel.class)
          .handler(new SessionInitializer(new SimpleChannelInboundHandler<ByteBuf>() {
            @Override protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
              // do nothing
            }
          }));

      Channel serverChannel = serverBootstrap.bind(new InetSocketAddress(0)).sync().channel();
      int port = ((InetSocketAddress) serverChannel.localAddress()).getPort();

      ChannelFuture channelFuture = clientBootstrap.connect(new InetSocketAddress(NetUtil.LOCALHOST, port));
      Assert.assertTrue(channelFuture.awaitUninterruptibly().isSuccess());

      Channel clientChannel = channelFuture.channel();
      clientChannel.writeAndFlush(Unpooled.copiedBuffer(new byte[] { (byte) SMFIC_OPTNEG.code()
          , 0x00, 0x00,        0x00,        0x06
          , 0x00, 0x00,        0x01, (byte) 0xFF
          , 0x00, 0x1F, (byte) 0xFF, (byte) 0xFF
      }));

      Assert.assertTrue(negotiateLatch.await(5, SECONDS));
    } finally {
      serverBootstrap.config().group().shutdownGracefully();
      serverBootstrap.config().childGroup().shutdownGracefully();
      clientBootstrap.config().group().shutdownGracefully();
    }
  }
}
