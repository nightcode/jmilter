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

import java.nio.charset.StandardCharsets;

import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.MilterState;
import org.nightcode.milter.codec.MilterPacket;

import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_BODYEOB;

public class EndOfBodyCommandProcessorTest {

  @Test public void testSubmit() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    String bodyText = "test data\r\n";
    
    MilterPacket packet1 = new MilterPacket(SMFIC_BODYEOB);
    MilterPacket packet2 = new MilterPacket(SMFIC_BODYEOB, bodyText.getBytes(StandardCharsets.UTF_8));

    EndOfBodyCommandProcessor processor = new EndOfBodyCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.EOM);
    EasyMock.expectLastCall().times(2);

    milterHandlerMock.eom(milterContextMock, null);
    EasyMock.expectLastCall().once();

    milterHandlerMock.eom(milterContextMock, bodyText);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet1);
    processor.submit(milterContextMock, packet2);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }
}
