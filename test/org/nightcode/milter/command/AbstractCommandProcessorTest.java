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

import java.util.function.Consumer;

import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterContextImpl;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.Hexs;
import org.nightcode.milter.util.ProtocolSteps;

import org.easymock.EasyMock;

class AbstractCommandProcessorTest {

  protected static final Hexs HEX = Hexs.hex();

  protected MilterContext context(MilterHandler handler) {
    return new MilterContextImpl(handler, Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS, null);
  }

  protected void execute(MilterPacket packet, CommandProcessor processor, Consumer<MilterContext> consumer) throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    consumer.accept(context);
    EasyMock.replay(handlerMock);
    processor.submit(context, packet);
    EasyMock.verify(handlerMock);
  }
}
