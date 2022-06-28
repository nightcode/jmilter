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

import org.nightcode.milter.MilterContext;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.IntMap;
import org.nightcode.milter.util.Log;

import static java.lang.String.format;
import static org.nightcode.milter.CommandCode.SMFIC_ABORT;

public final class CommandEngine {

  private static final CommandEngine INSTANCE = new CommandEngine();

  public static CommandEngine instance() {
    return INSTANCE;
  }

  private final IntMap<CommandProcessor> processors = new IntMap<>();

  private static void addProcessor(IntMap<CommandProcessor> processors, CommandProcessor commandHandler) {
    processors.put(commandHandler.command().code(), commandHandler);
  }

  private final CommandProcessor unknownCommandProcessor;

  private CommandEngine() {
    unknownCommandProcessor = new UnknownCommandProcessor();

    synchronized (processors) {
      addProcessor(processors, new AbortCommandProcessor());
      addProcessor(processors, new BodyCommandProcessor());
      addProcessor(processors, new ConnectCommandProcessor());
      addProcessor(processors, new MacrosCommandProcessor());
      addProcessor(processors, new EndOfBodyCommandProcessor());
      addProcessor(processors, new HeloCommandProcessor());
      addProcessor(processors, new QuitNcCommandProcessor());
      addProcessor(processors, new HeaderCommandProcessor());
      addProcessor(processors, new EnvfromCommandProcessor());
      addProcessor(processors, new EndOfHeadersCommandProcessor());
      addProcessor(processors, new OptnegCommandProcessor());
      addProcessor(processors, new QuitCommandProcessor());
      addProcessor(processors, new EnvrcptCommandProcessor());
      addProcessor(processors, new DataCommandProcessor());
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
      context.setSessionStep(SMFIC_ABORT);
      context.handler().abortSession(context, milterPacket);
    }
  }
}
