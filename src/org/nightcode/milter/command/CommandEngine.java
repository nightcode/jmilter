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

import java.util.logging.Level;
import java.util.logging.Logger;

public final class CommandEngine {

  private static final Logger LOGGER = Logger.getLogger(CommandEngine.class.getName());

  private final IntMap<CommandProcessor> processors = new IntMap<>();

  private static void addProcessor(CommandProcessor commandHandler, IntMap<CommandProcessor> processors) {
    processors.put(commandHandler.command(), commandHandler);
  }

  private final MilterHandler milterHandler;
  private final CommandProcessor unknownCommandProcessor;

  public CommandEngine(MilterHandler milterHandler) {
    this.milterHandler = milterHandler;

    unknownCommandProcessor = new UnknownCommandProcessor(milterHandler);

    synchronized (processors) {
      addProcessor(new OptnegCommandProcessor(milterHandler), processors);
      addProcessor(new ConnectCommandProcessor(milterHandler), processors);
      addProcessor(new HeloCommandProcessor(milterHandler), processors);
      addProcessor(new MacrosCommandProcessor(milterHandler), processors);
      addProcessor(new EnvfromCommandProcessor(milterHandler), processors);
      addProcessor(new EnvrcptCommandProcessor(milterHandler), processors);
      addProcessor(new HeaderCommandProcessor(milterHandler), processors);
      addProcessor(new EndOfHeadersCommandProcessor(milterHandler), processors);
      addProcessor(new BodyCommandProcessor(milterHandler), processors);
      addProcessor(new EndOfBodyCommandProcessor(milterHandler), processors);
      addProcessor(new AbortCommandProcessor(milterHandler), processors);
      addProcessor(new DataCommandProcessor(milterHandler), processors);
      addProcessor(new QuitCommandProcessor(milterHandler), processors);
      addProcessor(unknownCommandProcessor, processors);
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
      LOGGER.log(Level.WARNING, String.format("[%s] can't process milter packet: %s", context.id(), milterPacket), ex);
      context.setSessionState(MilterState.ABORT);
      milterHandler.abortSession(context, milterPacket);
    }
  }
}
