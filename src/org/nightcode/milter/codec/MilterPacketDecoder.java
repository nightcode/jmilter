/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.nightcode.milter.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Decodes a received {@link ByteBuf} into a {@link MilterPacket}.
 */
public class MilterPacketDecoder extends MessageToMessageDecoder<ByteBuf> {

  @Override protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
    final byte[] array;
    final int offset;
    final int length = msg.readableBytes();
    if (msg.hasArray()) {
      array = msg.array();
      offset = msg.arrayOffset() + msg.readerIndex();
    } else {
      array = new byte[length];
      msg.getBytes(msg.readerIndex(), array, 0, length);
      offset = 0;
    }

    MilterPacket milterPacket = new MilterPacket(array[offset], array, offset + MilterPacket.COMMAND_LENGTH
        , length - MilterPacket.COMMAND_LENGTH);
    out.add(milterPacket);
  }
}
