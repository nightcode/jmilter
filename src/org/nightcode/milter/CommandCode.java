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

package org.nightcode.milter;

import java.util.NoSuchElementException;

import static org.nightcode.milter.ProtocolSteps.NO_BODY;
import static org.nightcode.milter.ProtocolSteps.NO_CONNECT;
import static org.nightcode.milter.ProtocolSteps.NO_DATA;
import static org.nightcode.milter.ProtocolSteps.NO_EOH;
import static org.nightcode.milter.ProtocolSteps.NO_HEADERS;
import static org.nightcode.milter.ProtocolSteps.NO_HELO;
import static org.nightcode.milter.ProtocolSteps.NO_MAIL_FROM;
import static org.nightcode.milter.ProtocolSteps.NO_RECIPIENTS;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY_FOR_BODY;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY_FOR_CONNECT;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY_FOR_DATA;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY_FOR_EOH;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY_FOR_HEADERS;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY_FOR_HELO;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY_FOR_MAIL_FROM;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY_FOR_RECIPIENTS;
import static org.nightcode.milter.ProtocolSteps.NO_REPLY_FOR_UNKNOWN;
import static org.nightcode.milter.ProtocolSteps.NO_UNKNOWN;
import static org.nightcode.milter.ProtocolSteps.ZERO;
import static org.nightcode.milter.util.Properties.getLong;

public enum CommandCode implements Code {

  SMFIC_ABORT  ('A', ZERO,          NO_REPLY),
  SMFIC_BODY   ('B', NO_BODY,       NO_REPLY_FOR_BODY),
  SMFIC_CONNECT('C', NO_CONNECT,    NO_REPLY_FOR_CONNECT),
  SMFIC_MACRO  ('D', ZERO,          NO_REPLY),
  SMFIC_EOB    ('E', ZERO,          ZERO),
  SMFIC_HELO   ('H', NO_HELO,       NO_REPLY_FOR_HELO),
  SMFIC_QUIT_NC('K', ZERO,          NO_REPLY),
  SMFIC_HEADER ('L', NO_HEADERS,    NO_REPLY_FOR_HEADERS),
  SMFIC_MAIL   ('M', NO_MAIL_FROM,  NO_REPLY_FOR_MAIL_FROM),
  SMFIC_EOH    ('N', NO_EOH,        NO_REPLY_FOR_EOH),
  SMFIC_OPTNEG ('O', ZERO,          ZERO),
  SMFIC_QUIT   ('Q', ZERO,          NO_REPLY),
  SMFIC_RCPT   ('R', NO_RECIPIENTS, NO_REPLY_FOR_RECIPIENTS),
  SMFIC_DATA   ('T', NO_DATA,       NO_REPLY_FOR_DATA),
  SMFIC_UNKNOWN('U', NO_UNKNOWN,    NO_REPLY_FOR_UNKNOWN);

  private static final CommandCode[] CODES = new CommandCode['U' + 1];

  static {
    for (CommandCode code : CommandCode.values()) {
      CODES[code.code] = code;
    }
  }

  public static CommandCode valueOf(int code) {
    if (code < 'A' || code > 'U') {
      throw new IllegalArgumentException("invalid code value: " + code);
    }
    CommandCode commandCode = CODES[code];
    if (commandCode == null) {
      throw new NoSuchElementException("no command code with value: " + code);
    }
    return commandCode;
  }

  private final int code;
  private final int noStepBit;
  private final int noReplyBit;

  CommandCode(int code, int noStepBit, int noReplyBit) {
    this.code       = code;
    this.noStepBit  = noStepBit;
    this.noReplyBit = noReplyBit;
  }

  @Override public int code() {
    return code;
  }

  public int noStepBit() {
    return noStepBit;
  }

  public int noReplyBit() {
    return noReplyBit;
  }

  public long responseTimeoutMs() {
    return getLong(() -> "jmilter." + CommandCode.this.name() + ".responseTimeoutMs", 5_000);
  }
}
