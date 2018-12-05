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

package org.nightcode.milter;

import org.nightcode.common.base.Hexs;
import org.nightcode.milter.command.CommandProcessor;
import org.nightcode.milter.net.MilterPacket;
import org.nightcode.milter.net.MilterPacketSender;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.ProtocolSteps;

import java.io.IOException;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class AbstractMilterHandlerTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testNegotiate() throws MilterException, IOException {
    MilterPacketSender packetSenderMock = EasyMock.mock(MilterPacketSender.class);

    int mtaProtocolVersion = 6;
    Actions mtaActions = new Actions(new byte[] {0x00, 0x00, 0x01, (byte) 0xFF}, 0);
    ProtocolSteps mtaProtocolSteps = new ProtocolSteps(new byte[] { 0x00, 0x1F, (byte) 0xFF, (byte) 0xFF}, 0);

    Actions milterActions = Actions.builder()
        .changeFrom()
        .addRecipients()
        .deleteRecipients()
        .addHeader()
        .changeDeleteHeaders()
        .build();
    ProtocolSteps milterProtocolSteps = ProtocolSteps.DEF_PROTOCOL_STEPS;
    
    MilterHandler handler = new AbstractMilterHandler(milterActions, milterProtocolSteps) {
      @Override public void close(MilterContext context) {
        // do nothing
      }
    };

    MilterContext context = handler.createSession(packetSenderMock);

    Assert.assertNotNull(context.id());

    Capture<MilterPacket> packetCapture = EasyMock.newCapture();

    packetSenderMock.send(EasyMock.capture(packetCapture));
    EasyMock.expectLastCall().once();
    
    EasyMock.replay(packetSenderMock);

    context.setSessionState(MilterState.OPTION_NEGOTIATION);
    handler.negotiate(context, mtaProtocolVersion, mtaActions, mtaProtocolSteps);

    Assert.assertEquals(MilterContext.PROTOCOL_VERSION, context.milterProtocolVersion());
    Assert.assertEquals(milterActions, context.milterActions());
    Assert.assertEquals(milterProtocolSteps, context.milterProtocolSteps());

    Assert.assertEquals(mtaProtocolVersion, context.getMtaProtocolVersion());
    Assert.assertEquals(mtaActions, context.getMtaActions());
    Assert.assertEquals(mtaProtocolSteps, context.getMtaProtocolSteps());
    
    Assert.assertEquals(MilterContext.PROTOCOL_VERSION, context.getSessionProtocolVersion());
    Assert.assertEquals(milterProtocolSteps, context.getSessionProtocolSteps());
    
    MilterPacket target = packetCapture.getValue();
    Assert.assertEquals(CommandProcessor.SMFIC_OPTNEG, target.command());
    Assert.assertEquals("00000006"
            + HEX.fromByteArray(context.milterActions().array())
            + HEX.fromByteArray(context.getSessionProtocolSteps().array())  
        , HEX.fromByteArray(target.payload()));

    EasyMock.verify(packetSenderMock);
  }

  @Test public void testNegotiateAbortSession() throws MilterException {
    MilterPacketSender packetSenderMock = EasyMock.mock(MilterPacketSender.class);

    int mtaProtocolVersion = 6;
    Actions mtaActions = Actions.DEF_ACTIONS;
    ProtocolSteps mtaProtocolSteps = ProtocolSteps.DEF_PROTOCOL_STEPS;

    Actions milterActions = Actions.builder()
        .changeFrom()
        .addRecipients()
        .deleteRecipients()
        .addHeader()
        .changeDeleteHeaders()
        .build();
    ProtocolSteps milterProtocolSteps = ProtocolSteps.DEF_PROTOCOL_STEPS;

    MilterHandler handler = new AbstractMilterHandler(milterActions, milterProtocolSteps) {
      @Override public void close(MilterContext context) {
        // do nothing
      }
    };

    MilterContext context = handler.createSession(packetSenderMock);

    Assert.assertNotNull(context.id());

    packetSenderMock.close();
    EasyMock.expectLastCall().once();

    EasyMock.replay(packetSenderMock);

    context.setSessionState(MilterState.OPTION_NEGOTIATION);
    handler.negotiate(context, mtaProtocolVersion, mtaActions, mtaProtocolSteps);

    Assert.assertEquals(MilterContext.PROTOCOL_VERSION, context.milterProtocolVersion());
    Assert.assertEquals(milterActions, context.milterActions());
    Assert.assertEquals(milterProtocolSteps, context.milterProtocolSteps());

    Assert.assertEquals(mtaProtocolVersion, context.getMtaProtocolVersion());
    Assert.assertEquals(mtaActions, context.getMtaActions());
    Assert.assertNull(context.getMtaProtocolSteps());

    Assert.assertEquals(MilterContext.PROTOCOL_VERSION, context.getSessionProtocolVersion());
    Assert.assertNull(context.getSessionProtocolSteps());

    EasyMock.verify(packetSenderMock);
  }
  
  @Test public void testAbortSession() {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void close(MilterContext context) {
        // do nothing
      }
    };

    MilterPacket packet = MilterPacket.builder().command(CommandProcessor.SMFIC_CONNECT).build();

    EasyMock.expect(contextMock.getSessionState()).andReturn(MilterState.HEADERS).once();
    EasyMock.expect(contextMock.getSessionState()).andReturn(MilterState.EOM).once();
    EasyMock.expect(contextMock.getSessionState()).andReturn(MilterState.HEADERS).once();
    
    contextMock.destroy();
    EasyMock.expectLastCall().times(3);
    
    EasyMock.replay(contextMock);

    handler.abortSession(contextMock, packet);
    handler.abortSession(contextMock, packet);
    handler.abortSession(contextMock, packet);
    
    EasyMock.verify(contextMock);
  }
}
