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

import org.junit.Test;

import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_EOH;

public class EndOfHeadersCommandProcessorTest extends AbstractCommandProcessorTest {

  @Test public void testSubmit() throws MilterException {
    MilterPacket packet = new MilterPacket(SMFIC_EOH);

    execute(packet, new EndOfHeadersCommandProcessor(), ctx -> {
      try {
        ctx.handler().eoh(ctx);
        EasyMock.expectLastCall().once();
      } catch (MilterException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
