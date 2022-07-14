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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.jetbrains.annotations.NotNull;
import org.nightcode.milter.Actions;
import org.nightcode.milter.CommandCode;
import org.nightcode.milter.ProtocolFamily;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Futures;
import org.nightcode.milter.util.Log;

import static java.lang.String.format;
import static org.nightcode.milter.client.MilterPacketFactory.createBodyChunk;
import static org.nightcode.milter.client.MilterPacketFactory.createConnect;
import static org.nightcode.milter.client.MilterPacketFactory.createData;
import static org.nightcode.milter.client.MilterPacketFactory.createEnvfrom;
import static org.nightcode.milter.client.MilterPacketFactory.createEnvrcpt;
import static org.nightcode.milter.client.MilterPacketFactory.createHeader;
import static org.nightcode.milter.client.MilterPacketFactory.createHelo;
import static org.nightcode.milter.client.MilterPacketFactory.createMacro;

class MilterSessionImpl implements MilterSession {

  private final class ExceptionHandler extends ChannelInboundHandlerAdapter {
    private Throwable cause;

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      this.cause = cause;
      ctx.close();
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      if (cause != null) {
        closeFuture.completeExceptionally(cause);
      } else {
        closeFuture.complete(null);
      }
      super.channelInactive(ctx);
    }
  }

  private final Channel       channel;
  private final int           protocolVersion;
  private final Actions       milterActions;
  private final ProtocolSteps milterProtocolSteps;

  private final CompletableFuture<Void> closeFuture = new CompletableFuture<>();

  MilterSessionImpl(Channel channel, int protocolVersion, Actions milterActions, ProtocolSteps milterProtocolSteps, Set<Channel> channels) {
    this.channel             = channel;
    this.protocolVersion     = protocolVersion;
    this.milterActions       = milterActions;
    this.milterProtocolSteps = milterProtocolSteps;

    this.channel.pipeline().addLast("exceptionHandler", new ExceptionHandler());
    this.closeFuture.whenComplete((r, t) -> {
      Log.debug().log(MilterSessionImpl.this.getClass(), format("[%s] channel has been unregistered", channel.id().asLongText()));
      channels.remove(channel);
    });
  }

  @Override public CompletableFuture<MilterResponse> abort() {
    return send(MilterPacketFactory::createAbort);
  }

  @Override public CompletableFuture<MilterResponse> body(byte[] buffer) {
    return send(() -> createBodyChunk(buffer));
  }

  @Override public CompletableFuture<MilterResponse> connect(String hostname, ProtocolFamily family, int port, String address) {
    return send(() -> createConnect(hostname, family, port, address));
  }

  @Override public CompletableFuture<MilterResponse> connect(String hostname, ProtocolFamily family, int port, String address,
                                                             Macros macros) {
    return send(macros, () -> createConnect(hostname, family, port, address));
  }

  @Override public CompletableFuture<MilterResponse> macro(CommandCode code, Macros macros) {
    return send(() -> createMacro(code, macros));
  }

  @Override public CompletableFuture<MilterResponse> eob() {
    return send(MilterPacketFactory::createEob);
  }

  @Override public CompletableFuture<MilterResponse> eob(Macros macros) {
    return send(macros, MilterPacketFactory::createEob);
  }

  @Override public CompletableFuture<MilterResponse> helo(String helo) {
    return send(() -> createHelo(helo));
  }

  @Override public CompletableFuture<MilterResponse> helo(String helo, Macros macros) {
    return send(macros, () -> createHelo(helo));
  }

  @Override public CompletableFuture<MilterResponse> quitNc() {
    return send(MilterPacketFactory::createQuitNc);
  }

  @Override public CompletableFuture<MilterResponse> header(String name, String value) {
    return send(() -> createHeader(name, value));
  }

  @Override public CompletableFuture<MilterResponse> header(String name, String value, Macros macros) {
    return send(macros, () -> createHeader(name, value));
  }

  @Override public CompletableFuture<MilterResponse> envfrom(List<String> args) {
    return send(() -> createEnvfrom(args));
  }

  @Override public CompletableFuture<MilterResponse> envfrom(List<String> args, Macros macros) {
    return send(macros, () -> createEnvfrom(args));
  }

  @Override public CompletableFuture<MilterResponse> eoh() {
    return send(MilterPacketFactory::createEoh);
  }

  @Override public CompletableFuture<MilterResponse> eoh(Macros macros) {
    return send(macros, MilterPacketFactory::createEoh);
  }

  @Override public CompletableFuture<MilterResponse> envrcpt(List<String> args) {
    return send(() -> createEnvrcpt(args));
  }

  @Override public CompletableFuture<MilterResponse> envrcpt(List<String> args, Macros macros) {
    return send(macros, () -> createEnvrcpt(args));
  }

  @Override public CompletableFuture<Void> quit() {
    send(MilterPacketFactory::createQuit).whenComplete((r, t) -> channel.close());
    return closeFuture;
  }

  @Override public CompletableFuture<MilterResponse> data(byte[] payload) {
    return send(() -> createData(payload));
  }

  @Override public CompletableFuture<MilterResponse> data(byte[] payload, Macros macros) {
    return send(macros, () -> createData(payload));
  }

  @Override public String id() {
    return channel.id().asLongText();
  }

  @Override public int protocolVersion() {
    return protocolVersion;
  }

  @Override public Actions milterActions() {
    return milterActions;
  }

  @Override public ProtocolSteps milterProtocolSteps() {
    return milterProtocolSteps;
  }

  @Override public int compareTo(@NotNull MilterSession other) {
    return id().compareTo(other.id());
  }

  CompletableFuture<MilterResponse> send(Supplier<MilterPacket> supplier) {
    try {
      MilterPacket packet  = supplier.get();
      CommandCode  command = CommandCode.valueOf(packet.command());
      if ((milterProtocolSteps.bitmap() & command.noStepBit()) != 0) {
        Log.debug().log(getClass()
            , format("[%s] noStep bit has non-zero value for %s but attempt to send packet has been caught", id(), command));
        return CompletableFuture.completedFuture(MilterResponse.of(this));
      }

      CompletableFuture<MilterResponse> responseFuture = new CompletableFuture<>();

      MilterRequest request = new MilterRequest(command, new MilterPacket[] {packet}, this, channel, responseFuture);
      request.execute();

      return responseFuture;
    } catch (Exception ex) {
      return Futures.failedFuture(ex);
    }
  }

  CompletableFuture<MilterResponse> send(Macros macros, Supplier<MilterPacket> supplier) {
    try {
      MilterPacket packet  = supplier.get();
      CommandCode  command = CommandCode.valueOf(packet.command());
      MilterPacket macro   = createMacro(command, macros);

      if ((milterProtocolSteps.bitmap() & command.noStepBit()) != 0) {
        Log.debug().log(getClass()
            , format("[%s] noStep bit has non-zero value for %s but attempt to send packet has been caught", id(), command));
        return send(() -> macro);
      }

      CompletableFuture<MilterResponse> responseFuture = new CompletableFuture<>();

      MilterRequest request = new MilterRequest(command, new MilterPacket[] {macro, packet}, this, channel, responseFuture);
      request.execute();

      return responseFuture;
    } catch (Exception ex) {
      return Futures.failedFuture(ex);
    }
  }
}
