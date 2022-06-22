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

package org.nightcode.milter.codec;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.unix.PreferredDirectByteBufAllocator;

import org.junit.Assert;
import org.junit.Test;

public class MilterPacketDecoderTest {

  @Test public void testDecode() {
    EmbeddedChannel channel = new EmbeddedChannel(new MilterPacketDecoder());

    Random random = new Random(0);

    byte[] buffer = new byte[random.nextInt(128)];
    random.nextBytes(buffer);

    Assert.assertTrue(channel.writeInbound(Unpooled.copiedBuffer(buffer)));
    Assert.assertTrue(channel.finish());

    MilterPacket milterPacket = channel.readInbound();

    Assert.assertEquals(new MilterPacket(buffer[0], buffer, 1, buffer.length - 1), milterPacket);
  }

  @Test public void testDecodeDirect() {
    EmbeddedChannel channel = new EmbeddedChannel(new MilterPacketDecoder());

    Random random = new Random(0);

    byte[] buffer = new byte[random.nextInt(128)];
    random.nextBytes(buffer);

    ByteBuf byteBuf = new UnpooledDirectByteBuf(new PreferredDirectByteBufAllocator(), buffer.length, buffer.length);
    byteBuf.writeBytes(buffer);
    
    Assert.assertTrue(channel.writeInbound(byteBuf));
    Assert.assertTrue(channel.finish());

    MilterPacket milterPacket = channel.readInbound();

    Assert.assertEquals(new MilterPacket(buffer[0], buffer, 1, buffer.length - 1), milterPacket);
  }
}
