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

package org.nightcode.milter.net;

import java.util.UUID;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.command.CommandEngine;
import org.nightcode.milter.util.Log;

import static java.lang.String.format;

public class MilterChannelHandler extends SimpleChannelInboundHandler<MilterPacket> {

  private static final class MilterPacketSenderImpl implements MilterPacketSender {
    private final ChannelHandlerContext ctx;

    private MilterPacketSenderImpl(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }

    @Override public void close() {
      ctx.channel().attr(AttributeKey.valueOf("milterContext")).set(null);
      if (ctx.channel().isActive()) {
        ctx.close();
      }
    }

    @Override public void send(MilterPacket packet) {
      ctx.writeAndFlush(packet);
    }
  }

  private final MilterHandler milterHandler;
  private final CommandEngine commandManager;

  private final AttributeKey<MilterContext> milterContextAttrKey = AttributeKey.valueOf("milterContext");

  public MilterChannelHandler(MilterHandler milterHandler) {
    this.milterHandler = milterHandler;
    this.commandManager = new CommandEngine(milterHandler);
  }

  @Override protected void channelRead0(ChannelHandlerContext ctx, MilterPacket milterPacket) {
    MilterContext milterContext = getOrCreateMilterContext(ctx);
    commandManager.submit(milterContext, milterPacket);
  }

  @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    MilterContext milterContext = getMilterContext(ctx);
    if (milterContext != null) {
      milterHandler.closeSession(milterContext);
    }
    super.channelInactive(ctx);
  }

  @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    MilterContext milterContext = getMilterContext(ctx);
    UUID contextId = (milterContext != null) ? milterContext.id() : null;
    Log.warn().log(getClass(), format("[%s] channel exception: %s", contextId, cause.getMessage()), cause);

    if (milterContext != null) {
      milterHandler.abortSession(milterContext, null);
    }
    ctx.close();
  }

  private MilterContext getMilterContext(ChannelHandlerContext ctx) {
    return ctx.channel().attr(milterContextAttrKey).get();
  }

  private MilterContext getOrCreateMilterContext(ChannelHandlerContext ctx) {
    Attribute<MilterContext> attr = ctx.channel().attr(milterContextAttrKey);
    MilterContext milterContext = attr.get();
    if (milterContext == null) {
      milterContext = milterHandler.createSession(new MilterPacketSenderImpl(ctx));
      attr.set(milterContext);
    }
    return milterContext;
  }
}
