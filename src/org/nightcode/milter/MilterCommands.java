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

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.nightcode.milter.codec.MilterPacket;

public interface MilterCommands {

  /**
   * Message aborted.
   *
   * @param context milter context
   * @param packet aborted packet
   *
   * @throws MilterException if exception occurred
   */
  void abort(MilterContext context, @Nullable MilterPacket packet) throws MilterException;

  /**
   * Body block.
   *
   * @param context milter context
   * @param bodyChunk body chunk, up to 65535 bytes
   *
   * @throws MilterException if exception occurred
   */
  void body(MilterContext context, byte[] bodyChunk) throws MilterException;

  /**
   * Open SMTP connection.
   *
   * @param context milter context
   * @param hostname host name
   * @param family protocol family
   * @param port port number (SMFIA_INET or SMFIA_INET6 only)
   * @param address IP address (ASCII) or unix socket path
   *
   * @throws MilterException if exception occurred
   */
  void connect(MilterContext context, String hostname, int family, int port, @Nullable SocketAddress address) throws MilterException;

  /**
   * Define macros.
   *
   * @param context milter context
   * @param type command for which these macros apply
   * @param macros array of strings
   *
   * @throws MilterException if exception occurred
   */
  void macro(MilterContext context, int type, Map<String, String> macros) throws MilterException;

  /**
   * End of body.
   *
   * @param context milter context
   * @param bodyChunk final body chunk, up to 65535 bytes
   *
   * @throws MilterException if exception occurred
   */
  void eob(MilterContext context, @Nullable byte[] bodyChunk) throws MilterException;

  /**
   * HELO.
   *
   * @param context milter context
   * @param helohost HELO string
   *
   * @throws MilterException if exception occurred
   */
  void helo(MilterContext context, String helohost) throws MilterException;

  /**
   * QUIT but new connection follows.
   *
   * @param context milter context
   */
  void quitNc(MilterContext context);

  /**
   * Header.
   *
   * @param context milter context
   * @param headerName header name
   * @param headerValue header value
   *
   * @throws MilterException if exception occurred
   */
  void header(MilterContext context, String headerName, String headerValue) throws MilterException;

  /**
   * Envelope sender.
   *
   * @param context milter context
   * @param from sender and ESMTP arguments, if any
   *
   * @throws MilterException if exception occurred
   */
  void envfrom(MilterContext context, List<String> from) throws MilterException;

  /**
   * End of header.
   *
   * @param context milter context
   *
   * @throws MilterException if exception occurred
   */
  void eoh(MilterContext context) throws MilterException;

  /**
   * Option negotiation.
   *
   * @param context milter context
   * @param mtaProtoclVersion protocol version
   * @param mtaActions allowed actions
   * @param mtaProtocolSteps possible protocol content
   *
   * @throws MilterException if exception occurred
   */
  void optneg(MilterContext context, int mtaProtoclVersion, Actions mtaActions, ProtocolSteps mtaProtocolSteps) throws MilterException;

  /**
   * Quit milter communication.
   *
   * @param context milter context
   */
  void quit(MilterContext context);

  /**
   * Envelope recipient.
   *
   * @param context milter context
   * @param recipients recipient and ESMTP arguments, if any
   *
   * @throws MilterException if exception occurred
   */
  void envrcpt(MilterContext context, List<String> recipients) throws MilterException;

  /**
   * DATA command.
   *
   * @param context milter context
   * @param payload payload
   *
   * @throws MilterException if exception occurred
   */
  void data(MilterContext context, byte[] payload) throws MilterException;

  /**
   * Unknown SMTP command.
   *
   * @param context milter context
   * @param payload payload
   *
   * @throws MilterException if exception occurred
   */
  void unknown(MilterContext context, byte[] payload) throws MilterException;
}
