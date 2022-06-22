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

package org.nightcode.milter;

import java.util.NoSuchElementException;

public enum CommandCode implements Code {

  SMFIC_ABORT  ('A'), // Abort current filter checks
  SMFIC_BODY   ('B'), // Body chunk
  SMFIC_CONNECT('C'), // SMTP connection information
  SMFIC_MACRO  ('D'), // Define macros
  SMFIC_BODYEOB('E'), // End of body marker
  SMFIC_HELO   ('H'), // HELO/EHLO name
  SMFIC_QUIT_NC('K'), // QUIT but new connection follows
  SMFIC_HEADER ('L'), // Mail header
  SMFIC_MAIL   ('M'), // MAIL FROM: information
  SMFIC_EOH    ('N'), // End of headers marker
  SMFIC_OPTNEG ('O'), // Option negotiation
  SMFIC_QUIT   ('Q'), // Quit milter communication
  SMFIC_RCPT   ('R'), // RCPT TO: information
  SMFIC_DATA   ('T'), // DATA
  SMFIC_UNKNOWN('U'); // Any unknown command

  private static final CommandCode[] CODES = new CommandCode[90];

  static {
    for (CommandCode code : CommandCode.values()) {
      CODES[code.code] = code;
    }
  }

  public static CommandCode valueOf(int code) {
    if (code < 65 || code > 90) {
      throw new IllegalArgumentException("invalid code value: " + code);
    }
    CommandCode commandCode = CODES[code];
    if (commandCode == null) {
      throw new NoSuchElementException("no command code with value: " + code);
    }
    return commandCode;
  }

  private final int code;

  CommandCode(int code) {
    this.code = code;
  }

  @Override public int code() {
    return code;
  }
}
