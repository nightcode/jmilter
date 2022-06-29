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

import org.nightcode.milter.Actions;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterContextImpl;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.net.MilterPacketSender;

import org.junit.Assert;
import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_QUIT;

public class QuitCommandProcessorTest extends AbstractCommandProcessorTest {

  @Test public void testSubmit() throws MilterException {
    MilterHandler      handlerMock      = EasyMock.createMock(MilterHandler.class);
    MilterPacketSender packetSenderMock = EasyMock.mock(MilterPacketSender.class);
    MilterContext      context          = new MilterContextImpl(handlerMock, Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS, packetSenderMock);

    CommandProcessor processor = new QuitCommandProcessor();

    MilterPacket packet = new MilterPacket(SMFIC_QUIT);

    handlerMock.quit(context);
    EasyMock.expectLastCall().once();
    packetSenderMock.close();
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock, packetSenderMock);

    processor.submit(context, packet);
    Assert.assertEquals(SMFIC_QUIT, context.getSessionStep());

    EasyMock.verify(handlerMock, packetSenderMock);
  }
}
