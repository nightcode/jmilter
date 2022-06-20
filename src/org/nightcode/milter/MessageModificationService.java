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

import org.jetbrains.annotations.Nullable;

public interface MessageModificationService {

  int MILTER_CHUNK_SIZE = 65535;

  int SMFIR_ADDRCPT     = '+'; // Add recipient (modification action)
  int SMFIR_DELRCPT     = '-'; // Remove recipient (modification action)
  int SMFIR_ADDRCPT_PAR = '2'; // Add recipient (incl. ESMTP args)
  int SMFIR_SHUTDOWN    = '4'; // 421: shutdown (internal to MTA)
  int SMFIR_ACCEPT      = 'a'; // Accept message completely (accept/reject action)
  int SMFIR_REPLBODY    = 'b'; // Replace body (modification action)
  int SMFIR_CONTINUE    = 'c'; // Accept and keep processing (accept/reject action)
  int SMFIR_DISCARD     = 'd'; // Set discard flag for entire message (accept/reject action)
  int SMFIR_CHGFROM     = 'e'; // Change envelope sender (from)
  int SMFIR_CONN_FAIL   = 'f'; // Cause a connection failure
  int SMFIR_ADDHEADER   = 'h'; // Add header (modification action)
  int SMFIR_INSHEADER   = 'i'; // Insert header
  int SMFIR_SETSYMLIST  = 'l'; // Set list of symbols (macros)
  int SMFIR_CHGHEADER   = 'm'; // Change header (modification action)
  int SMFIR_PROGRESS    = 'p'; // Progress (asynchronous action)
  int SMFIR_QUARANTINE  = 'q'; // Quarantine message (modification action)
  int SMFIR_REJECT      = 'r'; // Reject command/recipient with a 5xx (accept/reject action)
  int SMFIR_SKIP        = 's'; // Skip
  int SMFIR_TEMPFAIL    = 't'; // Reject command/recipient with a 4xx (accept/reject action)
  int SMFIR_REPLYCODE   = 'y'; // Send specific Nxx reply message (accept/reject action)

  /**
   * Add a header to the message.
   *
   * @param name a header name
   * @param value a header value
   */
  void addHeader(MilterContext context, String name, String value) throws MilterException;

  /**
   * Change or delete a header.
   *
   * @param index a header index (1-based)
   * @param name a header name
   * @param value a header new value or NULL in case of delete action
   */
  void changeHeader(MilterContext context, int index, String name, @Nullable String value) throws MilterException;

  /**
   * Insert a header into the message.
   *
   * @param index a header index (1-based)
   * @param name a header name
   * @param value a header value
   */
  void insertHeader(MilterContext context, int index, String name, String value) throws MilterException;

  /**
   * Change the envelope sender address.
   *
   * @param from a new sender address
   * @param args a ESMTP arguments
   */
  void changeFrom(MilterContext context, String from, @Nullable String args) throws MilterException;

  /**
   * Add a new recipient's address to the current message.
   *
   * @param recipient a new recipient's address
   */
  void addRecipient(MilterContext context, String recipient) throws MilterException;

  /**
   * Add a recipient for the current message including ESMTP arguments.
   *
   * @param recipient a new recipient's address
   * @param args a new recipient's ESMTP parameters
   */
  void addRecipientEsmtpPar(MilterContext context, String recipient, String args) throws MilterException;

  /**
   * Remove a recipient from the current message's envelope.
   *
   * @param recipient a recipient address to be removed, a non-NULL, null-terminated string
   */
  void deleteRecipient(MilterContext context, String recipient) throws MilterException;

  /**
   * Replace message-body data, which does not have to be null-terminated.
   * If newBody is NULL, it is treated as having length == 0.
   * Body data should be in CR/LF form.
   *
   * @param body a new body data
   */
  void replaceBody(MilterContext context, byte[] body) throws MilterException;

  /**
   * Notify the MTA that an operation is still in progress.
   */
  void progress(MilterContext context) throws MilterException;

  /**
   * Quarantine the message using the given reason.
   *
   * @param reason a quarantine reason, a non-NULL and non-empty null-terminated string
   */
  void quarantine(MilterContext context, String reason) throws MilterException;
}
