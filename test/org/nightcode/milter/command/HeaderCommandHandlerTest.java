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

import static org.nightcode.milter.CommandCode.SMFIC_HEADER;

public class HeaderCommandHandlerTest extends AbstractCommandProcessorTest {

  @Test public void testSubmit() throws MilterException {
    MilterPacket packet = new MilterPacket(SMFIC_HEADER
        , HEX.toByteArray("46726f6d0020737570706f7274203c737570706f7274406578616d706c652e6f72673e00"));

    execute(packet, new HeaderCommandProcessor(), ctx -> {
      try {
        ctx.handler().header(ctx, "From", " support <support@example.org>");
        EasyMock.expectLastCall().once();
      } catch (MilterException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Test public void testCheckLastZeroTerm() throws MilterException {
    MilterPacket packet = new MilterPacket(SMFIC_HEADER
        , HEX.toByteArray("46726f6d0020737570706f7274203c737570706f7274406578616d706c652e6f72673e"));

    execute(packet, new HeaderCommandProcessor(), ctx -> {
      ctx.handler().abortSession(ctx, packet);
      EasyMock.expectLastCall().once();
    });
  }

  @Test public void testCheckWrongPacketLength() throws MilterException {
    MilterPacket packet = new MilterPacket(SMFIC_HEADER, HEX.toByteArray("46726f6d00"));

    execute(packet, new HeaderCommandProcessor(), ctx -> {
      ctx.handler().abortSession(ctx, packet);
      EasyMock.expectLastCall().once();
    });
  }
}
