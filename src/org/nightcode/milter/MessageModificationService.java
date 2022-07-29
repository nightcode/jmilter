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

import org.jetbrains.annotations.Nullable;

public interface MessageModificationService {

  /**
   * Add a header to the message.
   *
   * @param context milter context
   * @param name a header name
   * @param value a header value
   *
   * @throws MilterException if exception occurred
   */
  void addHeader(MilterContext context, String name, String value) throws MilterException;

  /**
   * Change or delete a header.
   *
   * @param context milter context
   * @param index a header index (1-based)
   * @param name a header name
   * @param value a header new value or NULL in case of delete action
   *
   * @throws MilterException if exception occurred
   */
  void changeHeader(MilterContext context, int index, String name, @Nullable String value) throws MilterException;

  /**
   * Insert a header into the message.
   *
   * @param context milter context
   * @param index a header index (1-based)
   * @param name a header name
   * @param value a header value
   *
   * @throws MilterException if exception occurred
   */
  void insertHeader(MilterContext context, int index, String name, String value) throws MilterException;

  /**
   * Change the envelope sender address.
   *
   * @param context milter context
   * @param from a new sender address
   * @param args a ESMTP arguments
   *
   * @throws MilterException if exception occurred
   */
  void changeFrom(MilterContext context, String from, @Nullable String args) throws MilterException;

  /**
   * Add a new recipient's address to the current message.
   *
   * @param context milter context
   * @param recipient a new recipient's address
   *
   * @throws MilterException if exception occurred
   */
  void addRecipient(MilterContext context, String recipient) throws MilterException;

  /**
   * Add a recipient for the current message including ESMTP arguments.
   *
   * @param context milter context
   * @param recipient a new recipient's address
   * @param args a new recipient's ESMTP parameters
   *
   * @throws MilterException if exception occurred
   */
  void addRecipientEsmtpPar(MilterContext context, String recipient, String args) throws MilterException;

  /**
   * Remove a recipient from the current message's envelope.
   *
   * @param context milter context
   * @param recipient a recipient address to be removed, a non-NULL, null-terminated string
   *
   * @throws MilterException if exception occurred
   */
  void deleteRecipient(MilterContext context, String recipient) throws MilterException;

  /**
   * Replace message-body data, which does not have to be null-terminated.
   * If newBody is NULL, it is treated as having length == 0.
   * Body data should be in CR/LF form.
   *
   * @param context milter context
   * @param body a new body data
   *
   * @throws MilterException if exception occurred
   */
  void replaceBody(MilterContext context, byte[] body) throws MilterException;

  /**
   * Notify the MTA that an operation is still in progress.
   *
   * @param context milter context
   *
   * @throws MilterException if exception occurred
   */
  void progress(MilterContext context) throws MilterException;

  /**
   * Quarantine the message using the given reason.
   *
   * @param context milter context
   * @param reason a quarantine reason, a non-NULL and non-empty null-terminated string
   *
   * @throws MilterException if exception occurred
   */
  void quarantine(MilterContext context, String reason) throws MilterException;
}
