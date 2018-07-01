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
import org.nightcode.milter.MilterException;
import org.nightcode.milter.net.MilterPacket;

public interface CommandProcessor {

  int SMFIC_ABORT = 'A'; // Abort current filter checks
  int SMFIC_BODY = 'B'; // Body chunk
  int SMFIC_CONNECT = 'C'; // SMTP connection information
  int SMFIC_MACRO = 'D'; // Define macros
  int SMFIC_BODYEOB = 'E'; // End of body marker
  int SMFIC_HELO = 'H'; // HELO/EHLO name
  int SMFIC_QUIT_NC = 'K'; // QUIT but new connection follows
  int SMFIC_HEADER = 'L'; // Mail header
  int SMFIC_MAIL = 'M'; // MAIL FROM: information
  int SMFIC_EOH = 'N'; // End of headers marker
  int SMFIC_OPTNEG = 'O'; // Option negotiation
  int SMFIC_QUIT = 'Q'; // Quit milter communication
  int SMFIC_RCPT = 'R'; // RCPT TO: information
  int SMFIC_DATA = 'T'; // DATA
  int SMFIC_UNKNOWN = 'U'; // Any unknown command

  /**
   * Returns command code.
   *
   * @return command code
   */
  int command();

  /**
   * Handles incoming Milter packet.
   *
   * @param context session context
   * @param packet Milter packet
   */
   void submit(MilterContext context, MilterPacket packet) throws MilterException;
}
