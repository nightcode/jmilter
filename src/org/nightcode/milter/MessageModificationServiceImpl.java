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

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.ByteArrays;
import org.nightcode.milter.util.ProtocolSteps;

import static org.nightcode.milter.ResponseCode.SMFIR_ADDHEADER;
import static org.nightcode.milter.ResponseCode.SMFIR_ADDRCPT;
import static org.nightcode.milter.ResponseCode.SMFIR_ADDRCPT_PAR;
import static org.nightcode.milter.ResponseCode.SMFIR_CHGFROM;
import static org.nightcode.milter.ResponseCode.SMFIR_CHGHEADER;
import static org.nightcode.milter.ResponseCode.SMFIR_DELRCPT;
import static org.nightcode.milter.ResponseCode.SMFIR_INSHEADER;
import static org.nightcode.milter.ResponseCode.SMFIR_PROGRESS;
import static org.nightcode.milter.ResponseCode.SMFIR_QUARANTINE;
import static org.nightcode.milter.ResponseCode.SMFIR_REPLBODY;

class MessageModificationServiceImpl implements MessageModificationService {

  MessageModificationServiceImpl() {
    // do nothing
  }

  @Override public void addHeader(MilterContext context, String name, String value) throws MilterException {
    header(context, SMFIR_ADDHEADER, -1, name, value);
  }

  @Override public void changeHeader(MilterContext context, int index, String name, @Nullable String value)
      throws MilterException {
    if (value == null) {
      value = "";
    }
    header(context, SMFIR_CHGHEADER, index, name, value);
  }

  @Override public void insertHeader(MilterContext context, int index, String name, String value)
      throws MilterException {
    header(context, SMFIR_INSHEADER, index, name, value);
  }

  @Override public void changeFrom(MilterContext context, String from, @Nullable String args) throws MilterException {
    send(SMFIR_CHGFROM, context, from, args);
  }

  @Override public void addRecipient(MilterContext context, String recipient) throws MilterException {
    send(SMFIR_ADDRCPT, context, recipient);
  }

  @Override public void addRecipientEsmtpPar(MilterContext context, String recipient, String args)
      throws MilterException {
    send(SMFIR_ADDRCPT_PAR, context, recipient, args);
  }

  @Override public void deleteRecipient(MilterContext context, String recipient) throws MilterException {
    send(SMFIR_DELRCPT, context, recipient);
  }

  @Override public void replaceBody(MilterContext context, byte[] body) throws MilterException {
    int length = Math.min(body.length, MILTER_CHUNK_SIZE);

    int offset = 0;
    while (offset < body.length) {
      MilterPacket packet = MilterPacket.builder()
          .command(SMFIR_REPLBODY)
          .payload(body, offset, length)
          .build();
      context.sendPacket(packet);
      offset += length;
      if (offset + length >= body.length) {
        length = body.length - offset;
      }
    }
  }

  @Override public void progress(MilterContext context) throws MilterException {
    MilterPacket packet = MilterPacket.builder()
        .command(SMFIR_PROGRESS)
        .build();

    context.sendPacket(packet);
  }

  @Override public void quarantine(MilterContext context, String reason) throws MilterException {
    send(SMFIR_QUARANTINE, context, reason);
  }

  private void header(MilterContext context, ResponseCode command, int index, String name, String value) throws MilterException {
    Objects.requireNonNull(name, "header name");
    Objects.requireNonNull(value, "header value");

    int payloadLength = 0;

    byte[] headerName = name.getBytes(StandardCharsets.UTF_8);
    payloadLength += (headerName.length + 1);

    if (value.length() > 0 && (context.getSessionProtocolSteps().bitmap() & ProtocolSteps.HEADER_VALUE_LEADING_SPACE)
        == ProtocolSteps.HEADER_VALUE_LEADING_SPACE) {
      value = " " + value;
    }

    byte[] headerValue = value.getBytes(StandardCharsets.UTF_8);
    payloadLength += (headerValue.length + 1);

    if (index >= 0) {
      payloadLength += 4;
    }

    int offset = 0;
    byte[] payload = new byte[payloadLength];

    if (index >= 0) {
      byte[] idx = ByteArrays.intToByteArray(index);
      System.arraycopy(idx, 0, payload, offset, idx.length);
      offset += idx.length;
    }

    System.arraycopy(headerName, 0, payload, offset, headerName.length);
    offset += (headerName.length + 1);

    System.arraycopy(headerValue, 0, payload, offset, headerValue.length);

    MilterPacket packet = MilterPacket.builder()
        .command(command)
        .payload(payload)
        .build();

    context.sendPacket(packet);
  }

  private void send(ResponseCode command, MilterContext context, String arg) throws MilterException {
    byte[] buf = arg.getBytes(StandardCharsets.UTF_8);
    byte[] payload = new byte[buf.length + 1];
    System.arraycopy(buf, 0, payload, 0, buf.length);

    MilterPacket packet = MilterPacket.builder()
        .command(command)
        .payload(payload)
        .build();
    context.sendPacket(packet);
  }

  private void send(ResponseCode command, MilterContext context, String arg1, String arg2) throws MilterException {
    int payloadLength = 0;

    byte[] buf1 = arg1.getBytes(StandardCharsets.UTF_8);
    payloadLength += (buf1.length + 1);

    byte[] buf2 = null;
    if (arg2 != null) {
      buf2 = arg2.getBytes(StandardCharsets.UTF_8);
      payloadLength += (buf2.length + 1);
    }

    int offset = 0;
    byte[] payload = new byte[payloadLength];

    System.arraycopy(buf1, 0, payload, 0, buf1.length);

    if (buf2 != null) {
      offset += (buf1.length + 1);
      System.arraycopy(buf2, 0, payload, offset, buf2.length);
    }

    MilterPacket packet = MilterPacket.builder()
        .command(command)
        .payload(payload)
        .build();
    context.sendPacket(packet);
  }
}
