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
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.Log;
import org.nightcode.milter.util.ProtocolSteps;

import static java.lang.String.format;

class OptnegCommandProcessor extends AbstractCommandHandler {

  private static final int PROTOCOL_MIN_VERSION = 2;

  OptnegCommandProcessor(MilterHandler handler) {
    super(handler);
  }

  @Override public int command() {
    return SMFIC_OPTNEG;
  }

  @Override public void submit(MilterContext context, MilterPacket packet) throws MilterException {
    context.setSessionState(MilterState.OPTION_NEGOTIATION);

    int payloadLength = packet.payload().length;
    if (payloadLength != 12) {
      Log.info().log(getClass(), format("[%s] wrong packet length: %s", context.id(), payloadLength));
      handler.abortSession(context, packet);
      return;
    }

    int mtaProtocolVersion = packet.payload()[3];
    if (mtaProtocolVersion < PROTOCOL_MIN_VERSION) {
      Log.info().log(getClass()
          , format("[%s] MTA protocol version too old %s < %s", context.id(), mtaProtocolVersion, PROTOCOL_MIN_VERSION));
      handler.abortSession(context, packet);
      return;
    }

    handler.negotiate(context, mtaProtocolVersion, new Actions(packet.payload(), 4)
        , new ProtocolSteps(packet.payload(), 8));
  }
}
