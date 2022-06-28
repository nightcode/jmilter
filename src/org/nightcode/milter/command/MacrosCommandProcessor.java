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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nightcode.milter.Code;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.MilterPacketUtil;

import static org.nightcode.milter.CommandCode.SMFIC_MACRO;

class MacrosCommandProcessor implements CommandProcessor {

  @Override public Code command() {
    return SMFIC_MACRO;
  }

  @Override public void submit(MilterContext context, MilterPacket packet) throws MilterException {
    int type = packet.payload()[0];
    List<String> list = MilterPacketUtil.splitByZeroTerm(packet.payload(), 1);
    Map<String, String> macros = new HashMap<>();
    for (int i = 0; i < list.size(); i += 2) {
      macros.put(list.get(i), list.get(i + 1));
    }
    context.handler().macro(context, type, macros);
  }
}
