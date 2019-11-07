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

import org.junit.Assert;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

public class Int32LenFrameEncoderTest {

  @Test public void testEncode() {
    EmbeddedChannel channel = new EmbeddedChannel(new Int32LenFrameEncoder());

    Random random = new Random(0);

    byte[] buffer = new byte[random.nextInt(128)];
    random.nextBytes(buffer);
    
    Assert.assertTrue(channel.writeOutbound(Unpooled.copiedBuffer(buffer)));
    Assert.assertTrue(channel.finish());

    ByteBuf written = Unpooled.buffer();

    for (;;) {
      ByteBuf buteBuf = channel.readOutbound();
      if (buteBuf == null) {
        break;
      }
      written.writeBytes(buteBuf);
      buteBuf.release();
    }

    Assert.assertEquals(buffer.length, written.readInt());

    byte[] actualPayload = new byte[written.readableBytes()];
    written.readBytes(actualPayload);
    written.release();

    Assert.assertArrayEquals(buffer, actualPayload);
  }
}
