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
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.net.MilterPacket;
import org.nightcode.milter.util.MilterPacketUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MacrosCommandProcessor extends AbstractCommandHandler {

  MacrosCommandProcessor(MilterHandler handler) {
    super(handler);
  }

  @Override public int command() {
    return SMFIC_MACRO;
  }

  @Override public void submit(MilterContext context, MilterPacket packet) {
    int type = packet.payload()[0];
    List<String> list = MilterPacketUtil.splitByZeroTerm(packet.payload(), 1);
    Map<String, String> macros = new HashMap<>();
    for (int i = 0; i < list.size(); i += 2) {
      macros.put(list.get(i), list.get(i + 1));
    }
    context.setMacros(type, macros);
  }
}
