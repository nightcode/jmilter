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

package org.nightcode.milter.command;

import org.nightcode.common.util.logging.LogManager;
import org.nightcode.common.util.logging.Logger;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.MilterState;
import org.nightcode.milter.net.MilterPacket;
import org.nightcode.milter.util.MilterPacketUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

class ConnectCommandProcessor extends AbstractCommandHandler {

  private static final Logger LOGGER = LogManager.getLogger(ConnectCommandProcessor.class);

  public static final int SMFIA_UNKNOWN = 'U'; // Unknown (NOTE: Omits "port" and "host" fields entirely)
  public static final int SMFIA_UNIX = 'L'; // Unix (AF_UNIX/AF_LOCAL) socket ("port" is 0)
  public static final int SMFIA_INET = '4'; // TCPv4 connection
  public static final int SMFIA_INET6 = '6'; // TCPv6 connection

  private static final int LAST_ZERO_TERM_LENGTH = 1;
  private static final int PORT_OFFSET = 2;

  ConnectCommandProcessor(MilterHandler handler) {
    super(handler);
  }

  @Override public int command() {
    return SMFIC_CONNECT;
  }

  @Override public void submit(MilterContext context, MilterPacket packet) throws MilterException {
    context.setSessionState(MilterState.CONNECT);
    if (!MilterPacketUtil.isLastZeroTerm(packet.payload())) {
      LOGGER.info("[%s] received invalid packet: %s", context.id(), packet);
      handler.abortSession(context, packet);
      return;
    }

    final int payloadLength = packet.payload().length;
    int i = MilterPacketUtil.indexOfZeroTerm(packet.payload());

    if ((i + LAST_ZERO_TERM_LENGTH) >= payloadLength) {
      LOGGER.info("[%s] wrong packet length: %s", context.id(), payloadLength);
      handler.abortSession(context, packet);
      return;
    }

    int offset = 0;
    String hostname = new String(packet.payload(), offset, i, StandardCharsets.UTF_8);
    i++;

    InetAddress address = null;
    int family = packet.payload()[i++];
    if (family == SMFIA_INET) {
      if (i + PORT_OFFSET >= payloadLength) {
        LOGGER.info("[%s] wrong packet length: %s", context.id(), payloadLength);
        handler.abortSession(context, packet);
        return;
      }
      i += PORT_OFFSET;
      offset = i;
      String stringAddress = new String(packet.payload(), offset, payloadLength - offset - LAST_ZERO_TERM_LENGTH
          , StandardCharsets.UTF_8);
      try {
        address = InetAddress.getByName(stringAddress);
      } catch (UnknownHostException ex) {
        LOGGER.info("[%s] invalid address value: %s", context.id(), stringAddress);
        handler.abortSession(context, packet);
        return;
      }
    }

    handler.connect(context, hostname, address);
  }
}
