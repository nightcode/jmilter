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
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CorruptedFrameException;

import org.junit.Assert;
import org.junit.Test;

public class Int32LenFrameDecoderTest {

  @Test public void testDecode() {
    EmbeddedChannel channel = new EmbeddedChannel(new Int32LenFrameDecoder());

    Random random = new Random(0);

    byte[] payload = new byte[random.nextInt(128)];
    random.nextBytes(payload);
    int div = random.nextInt(payload.length);

    Assert.assertFalse(channel.writeInbound(Unpooled.copyShort(payload.length >>> 16)));
    Assert.assertFalse(channel.writeInbound(Unpooled.copyShort(payload.length & 0xFFFF)));
    Assert.assertFalse(channel.writeInbound(Unpooled.copiedBuffer(payload, 0, div)));
    Assert.assertTrue(channel.writeInbound(Unpooled.copiedBuffer(payload, div, payload.length - div)));
    Assert.assertTrue(channel.finish());

    ByteBuf written = Unpooled.buffer();

    for (;;) {
      ByteBuf buffer = channel.readInbound();
      if (buffer == null) {
        break;
      }
      written.writeBytes(buffer);
      buffer.release();
    }

    byte[] actualPayload = new byte[written.readableBytes()];
    written.readBytes(actualPayload);
    written.release();

    Assert.assertArrayEquals(payload, actualPayload);
  }

  @Test public void testNegativeLength() {
    EmbeddedChannel channel = new EmbeddedChannel(new Int32LenFrameDecoder());

    try {
      channel.writeInbound(Unpooled.copyInt(-1));
      Assert.fail("must throw CorruptedFrameException");
    } catch (CorruptedFrameException ex) {
      Assert.assertEquals("negative length: -1", ex.getMessage());
    }
  }
}
