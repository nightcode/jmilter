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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Int32LenFrameEncoder extends MessageToByteEncoder<ByteBuf> {

  @Override protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
    int length = msg.readableBytes();
    out.ensureWritable(4 + length);
    out.writeInt(length);
    out.writeBytes(msg, msg.readerIndex(), length);
  }
}
