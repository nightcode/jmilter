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

import org.easymock.EasyMock;
import org.junit.Test;

public class DataCommandProcessorTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testSubmit() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    byte[] payload = HEX.toByteArray("54690031313331413641424542000000000154");
    MilterPacket packet = new MilterPacket((byte) 0x54, payload);

    DataCommandProcessor processor = new DataCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.DATA);
    EasyMock.expectLastCall().once();

    milterHandlerMock.data(milterContextMock, payload);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }
}
