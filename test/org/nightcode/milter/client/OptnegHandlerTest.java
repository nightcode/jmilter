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
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.handler.timeout.ReadTimeoutException;
import org.nightcode.milter.Actions;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.client.MilterSessionFactoryImpl.OptnegHandler;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Hexs;

import org.junit.Assert;
import org.junit.Test;

import static org.nightcode.milter.CommandCode.SMFIC_CONNECT;
import static org.nightcode.milter.client.MilterSessionFactoryImpl.SESSION_KEY;

public class OptnegHandlerTest {

  private static final Hexs HEX = Hexs.hex();
  
  @Test public void testConnection() {
    ConnectionFactory<LocalAddress> connectionFactory = new LocalConnectionFactory();
    MilterSessionFactoryBuilder<LocalAddress> builder = new MilterSessionFactoryBuilder<LocalAddress>()
        .factory(connectionFactory)
        .protocolVersion(6)
        .actions(Actions.DEF_ACTIONS)
        .protocolSteps(ProtocolSteps.DEF_PROTOCOL_STEPS);

    try (MilterSessionFactoryImpl<LocalAddress> factory = (MilterSessionFactoryImpl<LocalAddress>) builder.create()) {
      OptnegHandler handler = factory.createOptnegHandler();

      EmbeddedChannel channel = new EmbeddedChannel(handler);

      ChannelFuture cf = channel.connect(connectionFactory.remoteAddress());

      MilterPacket optneg = channel.readOutbound();
      Assert.assertArrayEquals(HEX.toByteArray("000000060000000F0000007F"), optneg.payload());
    
      channel.writeInbound(MilterPacketFactory.createOptneg(6, Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS));

      MilterSession session = channel.attr(SESSION_KEY).get();
      Assert.assertNotNull(session);

      Assert.assertTrue(cf.isSuccess());
    }
  }

  @Test public void testChannelReadWrongPacket() {
    ConnectionFactory<LocalAddress> connectionFactory = new LocalConnectionFactory();
    MilterSessionFactoryBuilder<LocalAddress> builder = new MilterSessionFactoryBuilder<LocalAddress>()
        .factory(connectionFactory)
        .protocolVersion(6)
        .actions(Actions.DEF_ACTIONS)
        .protocolSteps(ProtocolSteps.DEF_PROTOCOL_STEPS);

    try (MilterSessionFactoryImpl<LocalAddress> factory = (MilterSessionFactoryImpl<LocalAddress>) builder.create()) {
      OptnegHandler handler = factory.createOptnegHandler();

      EmbeddedChannel channel = new EmbeddedChannel(handler);

      ChannelFuture cf = channel.connect(connectionFactory.remoteAddress());
      channel.writeInbound(new Object());

      Assert.assertFalse(cf.isSuccess());
      Assert.assertTrue(cf.cause() instanceof IllegalStateException);
      Assert.assertTrue(Pattern.compile("\\[.*] received unexpected message of " + Object.class)
          .matcher(cf.cause().getMessage()).find());
    } 
  }

  @Test public void testChannelReadWrongCommand() {
    ConnectionFactory<LocalAddress> connectionFactory = new LocalConnectionFactory();
    MilterSessionFactoryBuilder<LocalAddress> builder = new MilterSessionFactoryBuilder<LocalAddress>()
        .factory(connectionFactory)
        .protocolVersion(6)
        .actions(Actions.DEF_ACTIONS)
        .protocolSteps(ProtocolSteps.DEF_PROTOCOL_STEPS);

    try (MilterSessionFactoryImpl<LocalAddress> factory = (MilterSessionFactoryImpl<LocalAddress>) builder.create()) {
      OptnegHandler handler = factory.createOptnegHandler();

      EmbeddedChannel channel = new EmbeddedChannel(handler);

      ChannelFuture cf = channel.connect(connectionFactory.remoteAddress());
      channel.writeInbound(MilterPacketFactory.createEoh());

      Assert.assertFalse(cf.isSuccess());
      Assert.assertTrue(cf.cause() instanceof IllegalStateException);
      Assert.assertTrue(Pattern.compile("\\[.*] received unexpected Milter Command 0x4e 'N'")
          .matcher(cf.cause().getMessage()).find());
    }
  }

  @Test public void testExceptionCaught() {
    ConnectionFactory<LocalAddress> connectionFactory = new LocalConnectionFactory();
    MilterSessionFactoryBuilder<LocalAddress> builder = new MilterSessionFactoryBuilder<LocalAddress>()
        .factory(connectionFactory)
        .protocolVersion(6)
        .actions(Actions.DEF_ACTIONS)
        .protocolSteps(ProtocolSteps.DEF_PROTOCOL_STEPS);

    try (MilterSessionFactoryImpl<LocalAddress> factory = (MilterSessionFactoryImpl<LocalAddress>) builder.create()) {
      OptnegHandler handler = factory.createOptnegHandler();

      EmbeddedChannel channel = new EmbeddedChannel(handler);

      ChannelFuture cf = channel.connect(connectionFactory.remoteAddress());

      IOException ioEx = new IOException("IO exception");
      channel.pipeline().fireExceptionCaught(ioEx);

      Assert.assertFalse(cf.isSuccess());
      Assert.assertSame(ioEx, cf.cause());
    }
  }

  @Test public void testReadTimeout() {
    ConnectionFactory<LocalAddress> connectionFactory = new LocalConnectionFactory();
    MilterSessionFactoryBuilder<LocalAddress> builder = new MilterSessionFactoryBuilder<LocalAddress>()
        .factory(connectionFactory)
        .protocolVersion(6)
        .actions(Actions.DEF_ACTIONS)
        .protocolSteps(ProtocolSteps.DEF_PROTOCOL_STEPS);

    try (MilterSessionFactoryImpl<LocalAddress> factory = (MilterSessionFactoryImpl<LocalAddress>) builder.create()) {
      OptnegHandler handler = factory.createOptnegHandler();

      EmbeddedChannel channel = new EmbeddedChannel(handler);

      ChannelFuture cf = channel.connect(connectionFactory.remoteAddress());

      MilterPacket optneg = channel.readOutbound();
      Assert.assertArrayEquals(HEX.toByteArray("000000060000000F0000007F"), optneg.payload());

      channel.advanceTimeBy(SMFIC_CONNECT.responseTimeoutMs(), TimeUnit.MILLISECONDS);
      channel.runScheduledPendingTasks();

      Assert.assertFalse(cf.isSuccess());
      Assert.assertEquals(ReadTimeoutException.INSTANCE, cf.cause());
    }
  }
}
