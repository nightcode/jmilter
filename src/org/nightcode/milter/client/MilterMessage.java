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

package org.nightcode.milter.client;

import java.util.Objects;

import org.nightcode.milter.CommandCode;
import org.nightcode.milter.codec.MilterPacket;

class MilterMessage {

  private final CommandCode    command;
  private final MilterPacket[] packets;
  private final MilterCallback callback;

  MilterMessage(CommandCode command, MilterPacket[] packets, MilterCallback callback) {
    Objects.requireNonNull(packets, "milter packets");
    this.command  = command;
    this.packets  = packets;
    this.callback = callback;
  }

  public MilterCallback callback() {
    return callback;
  }

  public CommandCode command() {
    return command;
  }

  public MilterPacket[] packets() {
    return packets;
  }
}
