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
import org.nightcode.milter.net.MilterPacket;
import org.nightcode.milter.util.Hexs;

import org.junit.Test;
import org.easymock.EasyMock;

public class EnvfromCommandProcessorTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testSubmit() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    List<String> list = new ArrayList<>();
    list.add("<support@example.org>");
    list.add("SIZE=1552");
    list.add("BODY=8BITMIME");
    
    MilterPacket packet = new MilterPacket((byte) 0x4d
        , HEX.toByteArray("3c737570706f7274406578616d706c652e6f72673e" 
        + "0053495a453d3135353200424f44593d384249544d494d4500"));

    EnvfromCommandProcessor processor = new EnvfromCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.MAIL_FROM);
    EasyMock.expectLastCall().once();

    milterHandlerMock.envfrom(milterContextMock, list);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }
}

