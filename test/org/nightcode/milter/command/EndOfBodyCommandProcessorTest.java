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

import java.nio.charset.StandardCharsets;

import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.codec.MilterPacket;

import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_EOB;

public class EndOfBodyCommandProcessorTest extends AbstractCommandProcessorTest {

  @Test public void testSubmit() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    String bodyText  = "test data\r\n";
    byte[] bodyChunk = bodyText.getBytes(StandardCharsets.UTF_8);

    MilterPacket packet1 = new MilterPacket(SMFIC_EOB);
    MilterPacket packet2 = new MilterPacket(SMFIC_EOB, bodyChunk);

    CommandProcessor processor = new EndOfBodyCommandProcessor();

    handlerMock.eom(context, null);
    EasyMock.expectLastCall().once();

    handlerMock.eom(context, bodyChunk);
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    processor.submit(context, packet1);
    processor.submit(context, packet2);

    EasyMock.verify(handlerMock);
  }
}
