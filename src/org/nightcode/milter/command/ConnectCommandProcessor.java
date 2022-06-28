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

package org.nightcode.milter.command;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import io.netty.channel.unix.DomainSocketAddress;
import org.nightcode.milter.Code;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Log;
import org.nightcode.milter.util.MilterPackets;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.nightcode.milter.CommandCode.SMFIC_CONNECT;
import static org.nightcode.milter.ProtocolFamily.SMFIA_INET;
import static org.nightcode.milter.ProtocolFamily.SMFIA_UNIX;
import static org.nightcode.milter.util.MilterPackets.ZERO_TERM_LENGTH;

class ConnectCommandProcessor implements CommandProcessor {

  private static final int PORT_OFFSET = 2;

  @Override public Code command() {
    return SMFIC_CONNECT;
  }

  @Override public void submit(MilterContext context, MilterPacket packet) throws MilterException {
    context.setSessionStep(SMFIC_CONNECT);

    if (!MilterPackets.isLastZeroTerm(packet.payload())) {
      Log.info().log(getClass(), format("[%s] received invalid packet: %s", context.id(), packet));
      context.handler().abortSession(context, packet);
      return;
    }

    final int payloadLength = packet.payload().length;
    int i = MilterPackets.indexOfZeroTerm(packet.payload());

    if ((i + ZERO_TERM_LENGTH) >= payloadLength) {
      Log.info().log(getClass(), format("[%s] wrong packet length=%s %s", context.id(), payloadLength, packet));
      context.handler().abortSession(context, packet);
      return;
    }

    int offset = 0;
    String hostname = new String(packet.payload(), offset, i, UTF_8);
    i++;

    int family = packet.payload()[i++];
    int port = 0;
    SocketAddress address = null;

    if (family == SMFIA_INET.code()) {
      if (i + PORT_OFFSET >= payloadLength) {
        Log.info().log(getClass(), format("[%s] wrong packet length=%s %s", context.id(), payloadLength, packet));
        context.handler().abortSession(context, packet);
        return;
      }
      port = ((packet.payload()[i++] & 0xFF) << 8) | (packet.payload()[i++] & 0xFF);
      offset = i;
      String stringAddress = new String(packet.payload(), offset, payloadLength - offset - ZERO_TERM_LENGTH, UTF_8);
      try {
        address = new InetSocketAddress(InetAddress.getByName(stringAddress), port);
      } catch (UnknownHostException ex) {
        Log.info().log(getClass(), format("[%s] invalid address value: %s", context.id(), stringAddress));
        context.handler().abortSession(context, packet);
        return;
      }
    } else if (family == SMFIA_UNIX.code()) {
      if (i + PORT_OFFSET >= payloadLength) {
        Log.info().log(getClass(), format("[%s] wrong packet length=%s %s", context.id(), payloadLength, packet));
        context.handler().abortSession(context, packet);
        return;
      }
      offset = i + PORT_OFFSET;
      String socketPath = new String(packet.payload(), offset, payloadLength - offset - ZERO_TERM_LENGTH, UTF_8);
      address = new DomainSocketAddress(socketPath);
    }

    context.handler().connect(context, hostname, family, port, address);
  }
}
