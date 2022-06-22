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

package org.nightcode.milter.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.nightcode.milter.MilterState;
import org.nightcode.milter.codec.MilterPacket;

import static org.nightcode.milter.ResponseCode.SMFIR_ACCEPT;
import static org.nightcode.milter.ResponseCode.SMFIR_CONTINUE;
import static org.nightcode.milter.ResponseCode.SMFIR_DISCARD;
import static org.nightcode.milter.ResponseCode.SMFIR_REJECT;
import static org.nightcode.milter.ResponseCode.SMFIR_SKIP;
import static org.nightcode.milter.ResponseCode.SMFIR_TEMPFAIL;

public enum MilterPacketUtil {
  ;

  /**
   * Continue processing the current connection, message, or recipient.
   */
  public static final MilterPacket SMFIS_CONTINUE = MilterPacket.builder().command(SMFIR_CONTINUE).build();

  /**
   * For a connection-oriented routine, reject this connection; call close().
   * For a message-oriented routine (except abort()), reject this message.
   * For a recipient-oriented routine, reject the current recipient (but continue processing the current message).
   */
  public static final MilterPacket SMFIS_REJECT = MilterPacket.builder().command(SMFIR_REJECT).build();

  /**
   * For a message- or recipient-oriented routine, accept this message, but silently discard it.
   * Should not be returned by a connection-oriented routine.
   */
  public static final MilterPacket SMFIS_DISCARD = MilterPacket.builder().command(SMFIR_DISCARD).build();

  /**
   * For a connection-oriented routine, accept this connection without further filter processing; call close().
   * For a message- or recipient-oriented routine, accept this message without further filtering.
   */
  public static final MilterPacket SMFIS_ACCEPT = MilterPacket.builder().command(SMFIR_ACCEPT).build();

  /**
   * Return a temporary failure, i.e., the corresponding SMTP command will return an appropriate 4xx status code.
   * For a message-oriented routine (except envfrom), fail for this message.
   * For a connection-oriented routine, fail for this connection; call close().
   * For a recipient-oriented routine, only fail for the current recipient; continue message processing.
   */
  public static final MilterPacket SMFIS_TEMPFAIL = MilterPacket.builder().command(SMFIR_TEMPFAIL).build();

  /**
   * Skip further callbacks of the same type in this transaction. Currently this return value
   * is only allowed in body(). It can be used if a milter has received sufficiently many
   * body chunks to make a decision, but still wants to invoke message modification functions
   * that are only allowed to be called from eom(). Note: the milter must negotiate this
   * behavior with the MTA, i.e., it must check whether the protocol action SMFIP_SKIP is available
   * and if so, the milter must request it.
   */
  public static final MilterPacket SMFIS_SKIP = MilterPacket.builder().command(SMFIR_SKIP).build();

  private static final byte ZERO_TERM = (byte) 0;

  private static final EnumSet<MilterState> MESSAGE_STATES = EnumSet.of(
        MilterState.MAIL_FROM
      , MilterState.RECIPIENTS
      , MilterState.DATA
      , MilterState.HEADERS
      , MilterState.EOH
      , MilterState.BODY
  );

  /**
   * Return the index of the first occurrence of the \0 in
   * the supplied buffer, or -1 if this buffer does not contain \0.
   *
   * @param buffer a buffer to be searched
   * @return the index of the first occurrence of the \0 in
   *         the supplied buffer, or -1 if this buffer does not contain \0
   */
  public static int indexOfZeroTerm(byte[] buffer) {
    return indexOfZeroTerm(buffer, 0);
  }

  /**
   * Returns the index of the first occurrence of the \0 starting from the offset
   * in the supplied buffer, or -1 if this buffer does not contain \0.
   *
   * @param buffer a buffer to be searched
   * @param offset starting position in the supplied buffer
   * @return the index of the first occurrence of the \0 starting from the offset
   *         in the supplied buffer, or -1 if this buffer does not contain \0
   */
  public static int indexOfZeroTerm(byte[] buffer, int offset) {
    if (offset < 0 || offset > buffer.length) {
      throw new IllegalArgumentException("illegal 'offset' value: " + offset
          + ", supplied buffer size: " + buffer.length);
    }

    for (int i = offset; i < buffer.length; i++) {
      if (buffer[i] == ZERO_TERM) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Indicates if the last byte in a buffer is \0.
   *
   * @param buffer a buffet to be checked
   * @return true if the last bytes in a buffer is \0, else return false
   */
  public static boolean isLastZeroTerm(byte[] buffer) {
    return buffer[buffer.length - 1] == ZERO_TERM;
  }

  public static boolean isMessageState(MilterState state) {
    return MESSAGE_STATES.contains(state);
  }

  /**
   * Splits the supplied buffer to strings, separated by \0.
   *
   * @param buffer the supplied buffer to be splitted
   * @return the list of strings
   */
  public static List<String> splitByZeroTerm(byte[] buffer) {
    return splitByZeroTerm(buffer, 0);
  }

  public static List<String> splitByZeroTerm(byte[] buffer, int offset) {
    if (offset < 0 || offset > buffer.length) {
      throw new IllegalArgumentException("illegal 'offset' value: " + offset + ", supplied buffer size: " + buffer.length);
    }
    List<String> result = new ArrayList<>();
    int i;
    while ((i = indexOfZeroTerm(buffer, offset)) > 0) {
      String str = new String(buffer, offset, i - offset, StandardCharsets.UTF_8);
      result.add(str);
      offset = ++i;
    }
    return result;
  }
}
