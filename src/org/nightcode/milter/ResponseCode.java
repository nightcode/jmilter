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

public enum ResponseCode implements Code {

  SMFIR_ADDRCPT    ('+', false), // Add recipient (modification action)
  SMFIR_DELRCPT    ('-', false), // Remove recipient (modification action)
  SMFIR_ADDRCPT_PAR('2', false), // Add recipient (incl. ESMTP args)
  SMFIR_SHUTDOWN   ('4', false), // 421: shutdown (internal to MTA)
  SMFIR_ACCEPT     ('a', true),  // Accept message completely (accept/reject action)
  SMFIR_REPLBODY   ('b', false), // Replace body (modification action)
  SMFIR_CONTINUE   ('c', true),  // Accept and keep processing (accept/reject action)
  SMFIR_DISCARD    ('d', true),  // Set discard flag for entire message (accept/reject action)
  SMFIR_CHGFROM    ('e', false), // Change envelope sender (from)
  SMFIR_CONN_FAIL  ('f', true),  // Cause a connection failure
  SMFIR_ADDHEADER  ('h', false), // Add header (modification action)
  SMFIR_INSHEADER  ('i', false), // Insert header
  SMFIR_SETSYMLIST ('l', false), // Set list of symbols (macros)
  SMFIR_CHGHEADER  ('m', false), // Change header (modification action)
  SMFIR_PROGRESS   ('p', false), // Progress (asynchronous action)
  SMFIR_QUARANTINE ('q', false), // Quarantine message (modification action)
  SMFIR_REJECT     ('r', true),  // Reject command/recipient with a 5xx (accept/reject action)
  SMFIR_SKIP       ('s', false), // Do not send more body chunks
  SMFIR_TEMPFAIL   ('t', true),  // Reject command/recipient with a 4xx (accept/reject action)
  SMFIR_REPLYCODE  ('y', true);  // Send specific Nxx reply message (accept/reject action)

  private static final ResponseCode[] CODES = new ResponseCode['y' + 1];

  static {
    for (ResponseCode code : ResponseCode.values()) {
      CODES[code.code] = code;
    }
  }

  public static ResponseCode valueOf(int code) {
    if (code < '+' || code > 'y') {
      throw new IllegalArgumentException("invalid code value: " + code);
    }
    ResponseCode responseCode = CODES[code];
    if (responseCode == null) {
      throw new NoSuchElementException("no response code with value: " + code);
    }
    return responseCode;
  }

  private final int code;
  private final boolean acceptReject;

  ResponseCode(int code, boolean acceptReject) {
    this.code = code;
    this.acceptReject = acceptReject;
  }

  public boolean acceptReject() {
    return acceptReject;
  }

  @Override public int code() {
    return code;
  }
}
