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

import java.util.UUID;

import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.MilterState;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Hexs;

import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_HEADER;

public class HeaderCommandHandlerTest {

  private static final Hexs HEX = Hexs.hex();

  private final UUID contextId = UUID.randomUUID();

  @Test public void testSubmit() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet = new MilterPacket(SMFIC_HEADER
        , HEX.toByteArray("46726f6d0020737570706f7274203c737570706f7274406578616d706c652e6f72673e00"));
    
    HeaderCommandProcessor processor = new HeaderCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.HEADERS);
    EasyMock.expectLastCall().once();

    milterHandlerMock.header(milterContextMock, "From", " support <support@example.org>");
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);
    
    processor.submit(milterContextMock, packet);
    
    EasyMock.verify(milterHandlerMock, milterContextMock);
  }

  @Test public void testCheckLastZeroTerm() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet = new MilterPacket(SMFIC_HEADER
        , HEX.toByteArray("46726f6d0020737570706f7274203c737570706f7274406578616d706c652e6f72673e"));

    HeaderCommandProcessor processor = new HeaderCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.HEADERS);
    EasyMock.expectLastCall().once();
    EasyMock.expect(milterContextMock.id()).andReturn(contextId).once();

    milterHandlerMock.abortSession(milterContextMock, packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }

  @Test public void testCheckWrongPacketLength() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet = new MilterPacket(SMFIC_HEADER, HEX.toByteArray("46726f6d00"));

    HeaderCommandProcessor processor = new HeaderCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.HEADERS);
    EasyMock.expectLastCall().once();
    EasyMock.expect(milterContextMock.id()).andReturn(contextId).once();

    milterHandlerMock.abortSession(milterContextMock, packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }
}
