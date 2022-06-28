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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

public class Int32LenFrameDecoder extends ByteToMessageDecoder {

  private static int readInt32(ByteBuf buffer) {
    if (!buffer.isReadable(4)) {
      return 0;
    }
    return buffer.readInt();
  }

  @Override protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    in.markReaderIndex();
    int preIndex = in.readerIndex();
    int length = readInt32(in);
    if (preIndex == in.readerIndex()) {
      return;
    }
    if (length < 0) {
      throw new CorruptedFrameException("negative length: " + length);
    }

    if (in.readableBytes() < length) {
      in.resetReaderIndex();
    } else {
      out.add(in.readRetainedSlice(length));
    }
  }
}
