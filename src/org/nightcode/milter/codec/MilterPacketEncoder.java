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

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * Encodes a requested {@link MilterPacket} into a {@link ByteBuf}.
 */
public class MilterPacketEncoder extends MessageToMessageEncoder<MilterPacket> {

  @Override protected void encode(ChannelHandlerContext ctx, MilterPacket msg, List<Object> out) {
    ByteBuf buffer = Unpooled.buffer(MilterPacket.COMMAND_LENGTH + msg.payload().length);
    buffer.writeByte(msg.command());
    buffer.writeBytes(msg.payload());
    out.add(buffer);
  }
}
