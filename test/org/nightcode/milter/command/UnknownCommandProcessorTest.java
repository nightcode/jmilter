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

import org.nightcode.milter.MilterException;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Hexs;

import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_UNKNOWN;

public class UnknownCommandProcessorTest extends AbstractCommandProcessorTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testSubmit() throws MilterException {
    MilterPacket packet = new MilterPacket(SMFIC_UNKNOWN, HEX.toByteArray("c0febebe"));

    execute(packet, new UnknownCommandProcessor(), ctx -> {
      try {
        ctx.handler().unknown(ctx, packet.payload());
        EasyMock.expectLastCall().once();
      } catch (MilterException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
