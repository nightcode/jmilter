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

import org.nightcode.common.base.Hexs;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.MilterState;
import org.nightcode.milter.net.MilterPacket;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.ProtocolSteps;

import java.util.UUID;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class OptnegCommandHandlerTest {

  private static final Hexs HEX = Hexs.hex();

  private final UUID contextId = UUID.randomUUID();

  @Test public void testSubmit() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    byte[] payload = HEX.toByteArray("00000006000001ff001fffff");
    MilterPacket packet = new MilterPacket((byte) 0x4f, payload);
    Actions actualActions = new Actions(payload, 4);
    ProtocolSteps actualProtocolSteps = new ProtocolSteps(payload, 8);

    OptnegCommandProcessor processor = new OptnegCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.OPTION_NEGOTIATION);
    EasyMock.expectLastCall().once();

    Capture<Actions> actionsCapture = EasyMock.newCapture();
    Capture<ProtocolSteps> protocolStepsCapture = EasyMock.newCapture();

    milterHandlerMock.negotiate(EasyMock.eq(milterContextMock), EasyMock.eq(6), EasyMock.capture(actionsCapture)
        , EasyMock.capture(protocolStepsCapture));
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    Assert.assertEquals(actualActions, actionsCapture.getValue());
    Assert.assertEquals(actualProtocolSteps, protocolStepsCapture.getValue());
    
    EasyMock.verify(milterHandlerMock, milterContextMock);
  }

  @Test public void testProtocolVersion() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet = new MilterPacket((byte) 0x4f, HEX.toByteArray("00000001000001ff001fffff"));

    OptnegCommandProcessor processor = new OptnegCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.OPTION_NEGOTIATION);
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

    MilterPacket packet = new MilterPacket((byte) 0x4f, HEX.toByteArray("00000006000001ff001fff"));

    OptnegCommandProcessor handler = new OptnegCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.OPTION_NEGOTIATION);
    EasyMock.expectLastCall().once();
    EasyMock.expect(milterContextMock.id()).andReturn(contextId).once();
    
    milterHandlerMock.abortSession(milterContextMock, packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    handler.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }
}
