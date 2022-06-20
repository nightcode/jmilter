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
import org.nightcode.milter.util.IntMap;
import org.nightcode.milter.util.Log;

import static java.lang.String.format;

public final class CommandEngine {

  private final IntMap<CommandProcessor> processors = new IntMap<>();

  private static void addProcessor(IntMap<CommandProcessor> processors, CommandProcessor commandHandler) {
    processors.put(commandHandler.command(), commandHandler);
  }

  private final MilterHandler milterHandler;
  private final CommandProcessor unknownCommandProcessor;

  public CommandEngine(MilterHandler milterHandler) {
    this.milterHandler = milterHandler;

    unknownCommandProcessor = new UnknownCommandProcessor(milterHandler);

    synchronized (processors) {
      addProcessor(processors, new OptnegCommandProcessor(milterHandler));
      addProcessor(processors, new ConnectCommandProcessor(milterHandler));
      addProcessor(processors, new HeloCommandProcessor(milterHandler));
      addProcessor(processors, new MacrosCommandProcessor(milterHandler));
      addProcessor(processors, new EnvfromCommandProcessor(milterHandler));
      addProcessor(processors, new EnvrcptCommandProcessor(milterHandler));
      addProcessor(processors, new HeaderCommandProcessor(milterHandler));
      addProcessor(processors, new EndOfHeadersCommandProcessor(milterHandler));
      addProcessor(processors, new BodyCommandProcessor(milterHandler));
      addProcessor(processors, new EndOfBodyCommandProcessor(milterHandler));
      addProcessor(processors, new AbortCommandProcessor(milterHandler));
      addProcessor(processors, new DataCommandProcessor(milterHandler));
      addProcessor(processors, new QuitCommandProcessor(milterHandler));
      addProcessor(processors, unknownCommandProcessor);
    }
  }

  public void submit(MilterContext context, MilterPacket milterPacket) {
    CommandProcessor processor = processors.get(milterPacket.command());
    if (processor == null) {
      processor = unknownCommandProcessor;
    }
    try {
      processor.submit(context, milterPacket);
    } catch (Exception ex) {
      Log.warn().log(getClass(), format("[%s] unable to process milter packet: %s", context.id(), milterPacket), ex);
      context.setSessionState(MilterState.ABORT);
      milterHandler.abortSession(context, milterPacket);
    }
  }
}
