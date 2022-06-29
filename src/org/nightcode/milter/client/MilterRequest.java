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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.TestOnly;
import org.nightcode.milter.CommandCode;
import org.nightcode.milter.ResponseCode;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Log;

import static java.lang.String.format;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY;

class MilterRequest implements MilterCallback {

  private final CommandCode                       command;
  private final MilterPacket[]                    packets;
  private final boolean                           needReply;
  private final MilterSession                     session;
  private final Channel                           channel;
  private final CompletableFuture<MilterResponse> responseFuture;
  private final List<MilterPacket>                responses;

  private ScheduledFuture<?> timeoutFuture;

  MilterRequest(CommandCode command, MilterPacket[] packets, MilterSession session, Channel channel,
                CompletableFuture<MilterResponse> responseFuture) {
    this.command        = command;
    this.packets        = packets;
    this.session        = session;
    this.channel        = channel;
    this.responseFuture = responseFuture;
    this.responses      = new ArrayList<>();
    this.needReply      = needReply();
  }

  public void execute() {
    MilterMessage message = new MilterMessage(command, packets, needReply ? this : null);
    ChannelFuture writeFuture = channel.writeAndFlush(message);
    writeFuture.addListener(this::writeListener);
  }

  private void writeListener(Future<? super Void> future) {
    if (future.isSuccess() && !needReply) {
      responseFuture.complete(MilterResponse.of(session));
    } else if (future.isSuccess()) {
      timeoutFuture = channel.eventLoop().schedule(this::onTimeout, command.responseTimeoutMs(), TimeUnit.MILLISECONDS);
      responseFuture.whenComplete((r, t) -> timeoutFuture.cancel(true));
    } else {
      completeExceptionally(future.cause());
    }
  }

  @Override public boolean isFinalAction(MilterPacket packet) {
    ResponseCode r = ResponseCode.valueOf(packet.command());
    return r.acceptReject();
  }

  @Override public void onAction(MilterPacket packet) {
    responses.add(packet);
    if (isFinalAction(packet)) {
      responseFuture.complete(MilterResponse.of(session, responses));
    }
  }

  @Override public void onFailure(Throwable cause) {
    if (timeoutFuture != null) {
      timeoutFuture.cancel(true);
    }
    completeExceptionally(cause);
  }

  @Override public String toString() {
    return "MilterRequest{"
        + "command=" + command
        + ", packets=" + Arrays.toString(packets)
        + '}';
  }

  @TestOnly
  ScheduledFuture<?> timeoutFuture() {
    return timeoutFuture;
  }

  private boolean needReply() {
    return (command.noReplyBit() & session.milterProtocolSteps().bitmap()) == 0 && command.noReplyBit() != NO_REPLY;
  }

  private void onTimeout() {
    Log.warn().log(getClass(), format("[%s] %s packet has not arrived within %s ms", session.id(), command, command.responseTimeoutMs()));
    completeExceptionally(ReadTimeoutException.INSTANCE);
  }

  private void completeExceptionally(Throwable cause) {
    responseFuture.completeExceptionally(cause);
  }
}
