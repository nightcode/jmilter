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

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.ProtocolSteps;

public interface MilterCommands {

  /**
   * Message aborted.
   */
  void abort(MilterContext context, @Nullable MilterPacket packet) throws MilterException;

  /**
   * Body block.
   */
  void body(MilterContext context, String bodyChunk) throws MilterException;

  /**
   * Open SMTP connection.
   */
  void connect(MilterContext context, String hostname, int family, int port, @Nullable SocketAddress address) throws MilterException;

  /**
   * Define macros.
   */
  void macro(MilterContext context, int type, Map<String, String> macros) throws MilterException;

  /**
   * End of message.
   */
  void eom(MilterContext context, @Nullable String bodyChunk) throws MilterException;

  /**
   * HELO.
   */
  void helo(MilterContext context, String helohost) throws MilterException;

  /**
   * QUIT but new connection follows.
   */
  void quitNc(MilterContext context);

  /**
   * Header.
   */
  void header(MilterContext context, String headerName, String headerValue) throws MilterException;

  /**
   * Envelope sender.
   */
  void envfrom(MilterContext context, List<String> from) throws MilterException;

  /**
   * End of header.
   */
  void eoh(MilterContext context) throws MilterException;

  /**
   * Option negotiation.
   */
  void optneg(MilterContext context, int mtaProtoclVersion, Actions mtaActions, ProtocolSteps mtaProtocolSteps) throws MilterException;

  /**
   * Quit milter communication.
   */
  void quit(MilterContext context);

  /**
   * Envelope recipient.
   */
  void envrcpt(MilterContext context, List<String> recipients) throws MilterException;

  /**
   * DATA command.
   */
  void data(MilterContext context, byte[] payload) throws MilterException;

  /**
   * Unknown SMTP command.
   */
  void unknown(MilterContext context, byte[] payload) throws MilterException;
}
