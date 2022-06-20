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
import org.nightcode.milter.net.MilterPacket;
import org.nightcode.milter.util.Hexs;

import org.junit.Test;
import org.easymock.EasyMock;

public class HeloCommandProcessorTest {

  private static final Hexs HEX = Hexs.hex();

  private final UUID contextId = UUID.randomUUID();

  @Test public void testSubmit() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet = new MilterPacket((byte) 0x48, HEX.toByteArray("6d61696c2e6578616d706c652e6f726700"));

    HeloCommandProcessor processor = new HeloCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.HELO);
    EasyMock.expectLastCall().once();

    milterHandlerMock.helo(milterContextMock, "mail.example.org");
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }

  @Test public void testCheckLastZeroTerm() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet = new MilterPacket((byte) 0x48, HEX.toByteArray("6d61696c2e6578616d706c652e6f7267"));

    HeloCommandProcessor processor = new HeloCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.HELO);
    EasyMock.expectLastCall().once();
    EasyMock.expect(milterContextMock.id()).andReturn(contextId).once();

    milterHandlerMock.abortSession(milterContextMock, packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }
}
