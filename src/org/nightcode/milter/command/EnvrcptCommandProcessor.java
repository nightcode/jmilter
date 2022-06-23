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

import java.util.List;

import org.nightcode.milter.Code;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterState;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.MilterPacketUtil;

import static org.nightcode.milter.CommandCode.SMFIC_RCPT;

class EnvrcptCommandProcessor implements CommandProcessor {

  @Override public Code command() {
    return SMFIC_RCPT;
  }

  @Override public void submit(MilterContext context, MilterPacket packet) throws MilterException {
    context.setSessionState(MilterState.RECIPIENTS);

    List<String> recipients = MilterPacketUtil.splitByZeroTerm(packet.payload());
    context.handler().envrcpt(context, recipients);
  }
}
