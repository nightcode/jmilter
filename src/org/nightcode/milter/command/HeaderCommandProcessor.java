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

import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.MilterState;
import org.nightcode.milter.net.MilterPacket;
import org.nightcode.milter.util.MilterPacketUtil;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

class HeaderCommandProcessor extends AbstractCommandHandler {

  private static final Logger LOGGER = Logger.getLogger(HeaderCommandProcessor.class.getName());

  private static final int LAST_ZERO_TERM_LENGTH = 1;

  HeaderCommandProcessor(MilterHandler handler) {
    super(handler);
  }

  @Override public int command() {
    return SMFIC_HEADER;
  }

  @Override public void submit(MilterContext context, MilterPacket packet) throws MilterException {
    context.setSessionState(MilterState.HEADERS);

    if (!MilterPacketUtil.isLastZeroTerm(packet.payload())) {
      LOGGER.log(Level.INFO, String.format("[%s] received invalid packet: %s", context.id(), packet));
      handler.abortSession(context, packet);
      return;
    }

    final int payloadLength = packet.payload().length;
    int i = MilterPacketUtil.indexOfZeroTerm(packet.payload());

    if ((i + LAST_ZERO_TERM_LENGTH) >= payloadLength) {
      LOGGER.log(Level.INFO, String.format("[%s] wrong packet length: %s", context.id(), payloadLength));
      handler.abortSession(context, packet);
      return;
    }

    int offset = 0;
    String headerName = new String(packet.payload(), offset, i, StandardCharsets.UTF_8);
    i++;

    offset = i;
    String headerValue
        = new String(packet.payload(), offset, payloadLength - offset - LAST_ZERO_TERM_LENGTH, StandardCharsets.UTF_8);

    handler.header(context, headerName, headerValue);
  }
}
