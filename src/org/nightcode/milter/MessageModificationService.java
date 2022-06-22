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
