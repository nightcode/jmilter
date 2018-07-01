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

import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.MilterState;
import org.nightcode.milter.net.MilterPacket;

import org.easymock.EasyMock;
import org.junit.Test;

public class QuitCommandProcessorTest {

  @Test public void testSubmit() {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet = new MilterPacket((byte) 0x63);

    QuitCommandProcessor processor = new QuitCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.QUIT);
    EasyMock.expectLastCall().once();

    milterContextMock.destroy();
    EasyMock.expectLastCall().once();
    
    milterHandlerMock.close(milterContextMock);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }
}
