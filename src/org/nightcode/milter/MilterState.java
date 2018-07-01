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

import org.nightcode.milter.util.ProtocolSteps;

public enum MilterState {

  NONE(0x00000000),
  INITIAL(0x00000000),
  OPTION_NEGOTIATION(0x00000000),
  CONNECT(ProtocolSteps.NO_REPLY_FOR_CONNECT),
  HELO(ProtocolSteps.NO_REPLY_FOR_HELO),
  MAIL_FROM(ProtocolSteps.NO_REPLY_FOR_MAIL_FROM),
  RECIPIENTS(ProtocolSteps.NO_REPLY_FOR_RECIPIENTS),
  DATA(ProtocolSteps.NO_REPLY_FOR_DATA),
  HEADERS(ProtocolSteps.NO_REPLY_FOR_HEADERS),
  EOH(ProtocolSteps.NO_REPLY_FOR_EOH),
  BODY(ProtocolSteps.NO_REPLY_FOR_BODY),
  EOM(0x00000000),
  QUIT(0x00000000),
  ABORT(0x00000000),
  UNKNOWN(ProtocolSteps.NO_REPLY_FOR_UNKNOWN),
  QUIT_NEW_CONNECTION(0x00000000),
  SKIP(0x00000000);

  private final int noRyplyBit;

  MilterState(int noRyplyBit) {
    this.noRyplyBit = noRyplyBit;
  }

  public int noReplyBit() {
    return noRyplyBit;
  }
}
