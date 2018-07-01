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

class HeloCommandProcessor extends AbstractCommandHandler {

  private static final Logger LOGGER = Logger.getLogger(HeloCommandProcessor.class.getName());

  HeloCommandProcessor(MilterHandler handler) {
    super(handler);
  }

  @Override public int command() {
    return SMFIC_HELO;
  }

  @Override public void submit(MilterContext context, MilterPacket packet) throws MilterException {
    context.setSessionState(MilterState.HELO);

    int i = MilterPacketUtil.indexOfZeroTerm(packet.payload());
    if (i < 0) {
      LOGGER.log(Level.INFO, String.format("[%s] received invalid packet: %s", context.id(), packet));
      handler.abortSession(context, packet);
      return;
    }

    String helohost = new String(packet.payload(), 0, i, StandardCharsets.UTF_8);
    handler.helo(context, helohost);
  }
}
