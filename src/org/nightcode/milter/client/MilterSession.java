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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.nightcode.milter.Actions;
import org.nightcode.milter.CommandCode;
import org.nightcode.milter.ProtocolFamily;
import org.nightcode.milter.ProtocolSteps;

public interface MilterSession extends Comparable<MilterSession> {

  /**
   * 'A' SMFIC_ABORT
   * Abort current filter checks
   * Expected response: NONE
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> abort();

  /**
   * 'B' SMFIC_BODY
   * Body chunk
   * Expected response: Accept/reject action
   *
   * @param buffer up to MILTER_CHUNK_SIZE (65535) bytes
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> body(byte[] buffer);

  /**
   * 'C' SMFIC_CONNECT
   * SMTP connection information
   * Expected response: Accept/reject action
   *
   * @param hostname hostname, NUL terminated
   * @param family   protocol family
   * @param port     port number (SMFIA_INET or SMFIA_INET6 only)
   * @param address  IP address (ASCII) or unix socket path, NUL terminated
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> connect(String hostname, ProtocolFamily family, int port, String address);

  /**
   * 'C' SMFIC_CONNECT
   * SMTP connection information
   * Expected response: Accept/reject action
   *
   * @param hostname hostname, NUL terminated
   * @param family   protocol family
   * @param port     port number (SMFIA_INET or SMFIA_INET6 only)
   * @param address  IP address (ASCII) or unix socket path, NUL terminated
   * @param macros   CONNECT macros
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> connect(String hostname, ProtocolFamily family, int port, String address,
                                            Macros macros);

  /**
   * 'D' SMFIC_MACRO
   * Define macros
   * Expected response: NONE
   *
   * @param code   command for which these macros apply
   * @param macros array of NUL-terminated strings, alternating between name of macro and value of macro.
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> macro(CommandCode code, Macros macros);

  /**
   * 'E' SMFIC_BODYEOB
   * End of body marker
   * Expected response: Zero or more modification actions, then accept/reject action
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> eob();

  /**
   * 'E' SMFIC_BODYEOB
   * End of body marker
   * Expected response: Zero or more modification actions, then accept/reject action
   *
   * @param macros SMFIC_BODYEOB macros
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> eob(Macros macros);

  /**
   * 'H' SMFIC_HELO
   * HELO/EHLO name
   * Expected response: Accept/reject action
   *
   * @param helo HELO string, NUL terminated
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> helo(String helo);

  /**
   * 'H' SMFIC_HELO
   * HELO/EHLO name
   * Expected response: Accept/reject action
   *
   * @param helo   HELO string, NUL terminated
   * @param macros HELO macros
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> helo(String helo, Macros macros);

  /**
   * 'K' SMFIC_QUIT_NC
   * Quit milter communication
   * Expected response: Keep milter connection
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> quitNc();

  /**
   * 'L' SMFIC_HEADER
   * Mail header
   * Expected response: Accept/reject action
   *
   * @param name  name of header, NUL terminated
   * @param value value of header, NUL terminated
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> header(String name, String value);

  /**
   * 'L' SMFIC_HEADER
   * Mail header
   * Expected response: Accept/reject action
   *
   * @param name   name of header, NUL terminated
   * @param value  value of header, NUL terminated
   * @param macros HEADER macros
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> header(String name, String value, Macros macros);

  /**
   * 'M' SMFIC_MAIL
   * MAIL FROM: information
   * Expected response: Accept/reject action
   *
   * @param args array of strings, NUL terminated (address at index 0).
   *             args[0] is sender, with &lt;&gt; qualification.
   *             args[1] and beyond are ESMTP arguments, if any.
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> envfrom(List<String> args);

  /**
   * 'M' SMFIC_MAIL
   * MAIL FROM: information
   * Expected response: Accept/reject action
   *
   * @param args   array of strings, NUL terminated (address at index 0).
   *               args[0] is sender, with &lt;&gt; qualification.
   *               args[1] and beyond are ESMTP arguments, if any.
   * @param macros MAIL macros
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> envfrom(List<String> args, Macros macros);

  /**
   * N' SMFIC_EOH
   * End of headers marker
   * Expected response: Accept/reject action
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> eoh();

  /**
   * N' SMFIC_EOH
   * End of headers marker
   * Expected response: Accept/reject action
   *
   * @param macros EOH macros
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> eoh(Macros macros);

  /**
   * 'R' SMFIC_RCPT
   * RCPT TO: information
   * Expected response: Accept/reject action
   *
   * @param args array of strings, NUL terminated (address at index 0).
   *             args[0] is recipient, with &lt;&gt; qualification.
   *             args[1] and beyond are ESMTP arguments, if any.
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> envrcpt(List<String> args);

  /**
   * 'R' SMFIC_RCPT
   * RCPT TO: information
   * Expected response: Accept/reject action
   *
   * @param args   array of strings, NUL terminated (address at index 0).
   *               args[0] is recipient, with &lt;&gt; qualification.
   *               args[1] and beyond are ESMTP arguments, if any.
   * @param macros RCPT macros
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> envrcpt(List<String> args, Macros macros);

  /**
   * 'Q' SMFIC_QUIT
   * Quit milter communication
   * Expected response: Close milter connection
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<Void> quit();

  /**
   * 'T' SMFIC_DATA
   * DATA
   * Expected response: Accept/reject action
   *
   * @param payload DATA content
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> data(byte[] payload);

  /**
   * 'T' SMFIC_DATA
   * DATA
   * Expected response: Accept/reject action
   *
   * @param payload DATA content
   * @param macros  DATA macros
   *
   * @return the CompletableFuture representing operation result
   */
  CompletableFuture<MilterResponse> data(byte[] payload, Macros macros);

  String id();

  int protocolVersion();

  Actions milterActions();

  ProtocolSteps milterProtocolSteps();
}
