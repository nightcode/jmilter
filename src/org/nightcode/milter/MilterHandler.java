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

import java.net.InetAddress;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.net.MilterPacketSender;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.ProtocolSteps;

public interface MilterHandler {

  /**
   * Open SMTP connection.
   */
  void connect(MilterContext context, String hostname, @Nullable InetAddress address) throws MilterException;

  /**
   * HELO.
   */
  void helo(MilterContext context, String helohost) throws MilterException;

  /**
   * Envelope sender.
   */
  void envfrom(MilterContext context, List<String> from) throws MilterException;

  /**
   * Envelope recipient.
   */
  void envrcpt(MilterContext context, List<String> recipients) throws MilterException;

  /**
   * Header.
   */
  void header(MilterContext context, String headerName, String headerValue) throws MilterException;

  /**
   * End of header.
   */
  void eoh(MilterContext context) throws MilterException;

  /**
   * Body block.
   */
  void body(MilterContext context, String bodyChunk) throws MilterException;

  /**
   * End of message.
   */
  void eom(MilterContext context, @Nullable String bodyChunk) throws MilterException;

  /**
   * Message aborted.
   */
  void abort(MilterContext context, MilterPacket packet) throws MilterException;

  /**
   * Connection cleanup.
   */
  void close(MilterContext context);

  /**
   * DATA command.
   */
  void data(MilterContext context, byte[] payload) throws MilterException;

  /**
   * Option negotiation.
   */
  void negotiate(MilterContext context, int mtaProtoclVersion, Actions mtaActions, ProtocolSteps mtaProtocolSteps)
      throws MilterException;

  /**
   * Unknown SMTP command.
   */
  void unknown(MilterContext context, byte[] payload) throws MilterException;

  void abortSession(MilterContext context, @Nullable MilterPacket packet);

  void closeSession(MilterContext context);

  MilterContext createSession(MilterPacketSender sender);
}
