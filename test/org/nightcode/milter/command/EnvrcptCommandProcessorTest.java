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

import java.util.ArrayList;
import java.util.List;

import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.MilterState;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Hexs;

import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_RCPT;

public class EnvrcptCommandProcessorTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testSubmit() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    List<String> list = new ArrayList<>();
    list.add("<client@example.org>");
    list.add("ORCPT=rfc822;client@example.org");
    
    MilterPacket packet = new MilterPacket(SMFIC_RCPT
        , HEX.toByteArray("3c636c69656e74406578616d706c652e6f72673e" 
        + "004f524350543d7266633832323b636c69656e74406578616d706c652e6f726700"));

    EnvrcptCommandProcessor processor = new EnvrcptCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.RECIPIENTS);
    EasyMock.expectLastCall().once();

    milterHandlerMock.envrcpt(milterContextMock, list);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }
}





