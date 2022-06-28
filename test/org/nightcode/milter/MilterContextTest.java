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

package org.nightcode.milter;

import java.io.IOException;

import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.net.MilterPacketSender;
import org.nightcode.milter.util.MilterPackets;

import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_CONNECT;

public class MilterContextTest {

  @Test public void testSendPacket() throws MilterException, IOException {
    MilterHandler      milterHandlerMock = EasyMock.mock(MilterHandler.class);
    MilterPacketSender packetSenderMock  = EasyMock.mock(MilterPacketSender.class);

    MilterContext context = new MilterContextImpl(milterHandlerMock, Actions.DEF_ACTIONS
        , ProtocolSteps.DEF_PROTOCOL_STEPS, packetSenderMock);

    MilterPacket packet = MilterPacket.builder()
        .command(SMFIC_CONNECT)
        .build();

    packetSenderMock.send(packet);
    EasyMock.expectLastCall().once();

    context.setSessionProtocolSteps(ProtocolSteps.DEF_PROTOCOL_STEPS);
    context.setSessionStep(SMFIC_CONNECT);
    
    EasyMock.replay(packetSenderMock);

    context.sendPacket(packet);

    EasyMock.verify(packetSenderMock);
  }

  @Test public void testSendPacketNr() throws MilterException, IOException {
    MilterHandler      milterHandlerMock = EasyMock.mock(MilterHandler.class);
    MilterPacketSender packetSenderMock  = EasyMock.mock(MilterPacketSender.class);

    MilterContext context = new MilterContextImpl(milterHandlerMock, Actions.DEF_ACTIONS
        , ProtocolSteps.builder().noReplyForConnect().build(), packetSenderMock);

    MilterPacket packet = MilterPacket.builder()
        .command(SMFIC_CONNECT)
        .build();

    packetSenderMock.send(MilterPackets.SMFIS_CONTINUE);
    EasyMock.expectLastCall().once();

    context.setMtaProtocolSteps(ProtocolSteps.DEF_PROTOCOL_STEPS);

    context.setSessionProtocolSteps(ProtocolSteps.builder().noReplyForConnect().build());
    context.setSessionStep(SMFIC_CONNECT);

    EasyMock.replay(packetSenderMock);

    context.sendPacket(packet);

    context.setMtaProtocolSteps(ProtocolSteps.builder().noReplyForConnect().build());
    context.sendPacket(packet);

    EasyMock.verify(packetSenderMock);
  }

  @Test public void testSendContinue() throws MilterException, IOException {
    MilterHandler      milterHandlerMock = EasyMock.mock(MilterHandler.class);
    MilterPacketSender packetSenderMock  = EasyMock.mock(MilterPacketSender.class);

    MilterContext context
        = new MilterContextImpl(milterHandlerMock, Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS, packetSenderMock);

    packetSenderMock.send(MilterPackets.SMFIS_CONTINUE);
    EasyMock.expectLastCall().once();

    context.setSessionProtocolSteps(ProtocolSteps.DEF_PROTOCOL_STEPS);
    context.setSessionStep(SMFIC_CONNECT);

    EasyMock.replay(packetSenderMock);

    context.sendContinue();

    EasyMock.verify(packetSenderMock);
  }

  @Test public void testDestroy() {
    MilterHandler      milterHandlerMock = EasyMock.mock(MilterHandler.class);
    MilterPacketSender packetSenderMock  = EasyMock.mock(MilterPacketSender.class);

    MilterContext context = new MilterContextImpl(milterHandlerMock, Actions.DEF_ACTIONS
        , ProtocolSteps.builder().noReplyForConnect().build(), packetSenderMock);

    packetSenderMock.close();
    EasyMock.expectLastCall().once();
    
    EasyMock.replay(packetSenderMock);

    context.destroy();

    EasyMock.verify(packetSenderMock);
  }
}
