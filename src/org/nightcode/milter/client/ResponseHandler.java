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

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.PromiseCombiner;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Log;

import static java.lang.String.format;

/**
 * IMPORTANT! not thread safe
 */
class ResponseHandler extends ChannelDuplexHandler {

  private static String id(ChannelHandlerContext ctx) {
    return ctx.channel().id().asLongText();
  }

  private MilterCallback callback;

  @Override public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
    if (msg instanceof MilterMessage) {
      write(ctx, (MilterMessage) msg, promise);
    } else {
      promise.setFailure(new IllegalArgumentException("[" + id(ctx) + "] unsupported message type " + msg.getClass()));
    }
  }

  @Override public void channelRead(ChannelHandlerContext ctx, Object msg) {
    MilterCallback tmpCallback = callback;
    if (tmpCallback == null) {
      Log.warn().log(getClass(), format("[%s] received packet %s but no callback has been registered", id(ctx), msg));
      return;
    }

    try {
      MilterPacket packet = (MilterPacket) msg;
      if (tmpCallback.isFinalAction(packet)) {
        Log.debug().log(getClass(), () -> format("[%s] received final action %s", id(ctx), packet));
        callback = null;
      } else {
        Log.debug().log(getClass(), () -> format("[%s] received non-final action %s", id(ctx), packet));
      }
      tmpCallback.onAction(packet);
    } catch (Exception ex) {
      tmpCallback.onFailure(ex);
    }
  }

  @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    MilterCallback tmpCallback = callback;
    if (tmpCallback != null) {
      tmpCallback.onFailure(cause);
    }
    callback = null;
    ctx.close();
  }

  @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    MilterCallback tmpCallback = callback;
    if (tmpCallback != null) {
      tmpCallback.onFailure(new ChannelException("[" + id(ctx) + "] connection to Milter has been closed"));
    }
    callback = null;
    super.channelInactive(ctx);
  }

  private void write(ChannelHandlerContext ctx, MilterMessage message, ChannelPromise promise) {
    if (callback != null) {
      promise.tryFailure(new IllegalStateException(format("[%s] previous request %s wasn't correctly completed", id(ctx), callback)));
      return;
    }

    callback = message.callback();

    if (message.packets().length == 1) {
      Log.debug().log(getClass(), () -> format("[%s] writing command %s", id(ctx), message.packets()[0]));
      ctx.writeAndFlush(message.packets()[0], promise);
    } else {
      PromiseCombiner combiner = new PromiseCombiner(ctx.executor());
      for (MilterPacket command : message.packets()) {
        Log.debug().log(getClass(), () -> format("[%s] writing command %s", id(ctx), command));
        combiner.add(ctx.write(command));
      }
      combiner.finish(promise);
      ctx.flush();
    }
  }
}
