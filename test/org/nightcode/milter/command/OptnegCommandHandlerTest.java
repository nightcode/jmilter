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

import org.nightcode.milter.MilterException;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.ProtocolSteps;

import org.junit.Assert;
import org.junit.Test;
import org.easymock.Capture;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_OPTNEG;

public class OptnegCommandHandlerTest extends AbstractCommandProcessorTest {

  @Test public void testSubmit() throws MilterException {
    byte[] payload = HEX.toByteArray("00000006000001ff001fffff");
    MilterPacket packet = new MilterPacket(SMFIC_OPTNEG, payload);
    Actions actualActions = new Actions(payload, 4);
    ProtocolSteps actualProtocolSteps = new ProtocolSteps(payload, 8);

    Capture<Actions> actionsCapture = EasyMock.newCapture();
    Capture<ProtocolSteps> protocolStepsCapture = EasyMock.newCapture();

    execute(packet, new OptnegCommandProcessor(), ctx -> {
      try {
        ctx.handler().optneg(EasyMock.eq(ctx), EasyMock.eq(6), EasyMock.capture(actionsCapture)
            , EasyMock.capture(protocolStepsCapture));
        EasyMock.expectLastCall().once();
      } catch (MilterException e) {
        throw new RuntimeException(e);
      }
    });

    Assert.assertEquals(actualActions, actionsCapture.getValue());
    Assert.assertEquals(actualProtocolSteps, protocolStepsCapture.getValue());
  }

  @Test public void testProtocolVersion() throws MilterException {
    MilterPacket packet = new MilterPacket(SMFIC_OPTNEG, HEX.toByteArray("00000001000001ff001fffff"));

    execute(packet, new OptnegCommandProcessor(), ctx -> {
      ctx.handler().abortSession(ctx, packet);
      EasyMock.expectLastCall().once();
    });
  }
  
  @Test public void testCheckWrongPacketLength() throws MilterException {
    MilterPacket packet = new MilterPacket(SMFIC_OPTNEG, HEX.toByteArray("00000006000001ff001fff"));

    execute(packet, new OptnegCommandProcessor(), ctx -> {
      ctx.handler().abortSession(ctx, packet);
      EasyMock.expectLastCall().once();
    });
  }
}
