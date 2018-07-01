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

import org.nightcode.common.base.Hexs;

/**
 * Protocol steps.
 */
public final class ProtocolSteps {

  public static final class Builder {
    private int bitmap = 0x00000000;

    private Builder() {
      // do nothing
    }

    public ProtocolSteps build() {
      return new ProtocolSteps(this);
    }

    /**
     * MTA should not send connect info.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noConnect() {
      bitmap |= NO_CONNECT;
      return this;
    }

    /**
     * MTA should not send HELO info.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noHelo() {
      bitmap |= NO_HELO;
      return this;
    }

    /**
     * MTA should not send MAIL FROM info.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noMailFrom() {
      bitmap |= NO_MAIL_FROM;
      return this;
    }

    /**
     * MTA should not send RCPT info.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noRecipients() {
      bitmap |= NO_RECIPIENTS;
      return this;
    }

    /**
     * MTA should not send body.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noBody() {
      bitmap |= NO_BODY;
      return this;
    }

    /**
     * MTA should not send headers.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noHeaders() {
      bitmap |= NO_HEADERS;
      return this;
    }

    /**
     * MTA should not send EOH.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noEoh() {
      bitmap |= NO_EOH;
      return this;
    }

    /**
     * No reply for headers.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noReplyForHeaders() {
      bitmap |= NO_REPLY_FOR_HEADERS;
      return this;
    }

    /**
     * MTA should not send unknown commands.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noUnknownCommands() {
      bitmap |= NO_UNKNOWN;
      return this;
    }

    /**
     * MTA should not send DATA.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noData() {
      bitmap |= NO_DATA;
      return this;
    }

    /**
     * MTA understands SMFIS_SKIP.
     *
     * @return the current {@link Builder} instance
     */
    public Builder understandSkip() {
      bitmap |= UNDERSTAND_SKIP;
      return this;
    }

    /**
     * MTA should also send rejected RCPTs.
     *
     * @return the current {@link Builder} instance
     */
    public Builder sendRejectedRecipients() {
      bitmap |= SEND_REJECT_RECIPIENTS;
      return this;
    }

    /**
     * No reply for connect.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noReplyForConnect() {
      bitmap |= NO_REPLY_FOR_CONNECT;
      return this;
    }

    /**
     * No reply for HELO.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noReplyForHelo() {
      bitmap |= NO_REPLY_FOR_HELO;
      return this;
    }

    /**
     * No reply for MAIL.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noReplyForMailFrom() {
      bitmap |= NO_REPLY_FOR_MAIL_FROM;
      return this;
    }

    /**
     * No reply for RCPT.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noReplyForRecipients() {
      bitmap |= NO_REPLY_FOR_RECIPIENTS;
      return this;
    }

    /**
     * No reply for DATA.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noReplyForData() {
      bitmap |= NO_REPLY_FOR_DATA;
      return this;
    }

    /**
     * No reply for UNKN.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noReplyForUnknown() {
      bitmap |= NO_REPLY_FOR_UNKNOWN;
      return this;
    }

    /**
     * No reply for EOH.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noReplyForEoh() {
      bitmap |= NO_REPLY_FOR_EOH;
      return this;
    }

    /**
     * No reply for body chunk.
     *
     * @return the current {@link Builder} instance
     */
    public Builder noReplyForBodyChunk() {
      bitmap |= NO_REPLY_FOR_BODY;
      return this;
    }

    /**
     * Header value leading space.
     *
     * @return the current {@link Builder}  instance
     */
    public Builder headerValueLeadingSpace() {
      bitmap |= HEADER_VALUE_LEADING_SPACE;
      return this;
    }
  }

  public static final int NO_CONNECT = 0x00000001;
  public static final int NO_HELO = 0x00000002;
  public static final int NO_MAIL_FROM = 0x00000004;
  public static final int NO_RECIPIENTS = 0x00000008;
  public static final int NO_BODY = 0x00000010;
  public static final int NO_HEADERS = 0x00000020;
  public static final int NO_EOH = 0x00000040;
  public static final int NO_REPLY_FOR_HEADERS = 0x00000080;
  public static final int NO_UNKNOWN = 0x00000100;
  public static final int NO_DATA = 0x00000200;
  public static final int UNDERSTAND_SKIP = 0x00000400;
  public static final int SEND_REJECT_RECIPIENTS = 0x00000800;
  public static final int NO_REPLY_FOR_CONNECT = 0x00001000;
  public static final int NO_REPLY_FOR_HELO = 0x00002000;
  public static final int NO_REPLY_FOR_MAIL_FROM = 0x00004000;
  public static final int NO_REPLY_FOR_RECIPIENTS = 0x00008000;
  public static final int NO_REPLY_FOR_DATA = 0x00010000;
  public static final int NO_REPLY_FOR_UNKNOWN = 0x00020000;
  public static final int NO_REPLY_FOR_EOH = 0x00040000;
  public static final int NO_REPLY_FOR_BODY = 0x00080000;
  public static final int HEADER_VALUE_LEADING_SPACE = 0x00100000;

  // The protocol steps of V1 filter
  public static final ProtocolSteps DEF_PROTOCOL_STEPS = ProtocolSteps.builder()
      .noConnect()
      .noHelo()
      .noMailFrom()
      .noRecipients()
      .noBody()
      .noHeaders()
      .noEoh()
      .build();

  private static final Hexs HEX = Hexs.hex();

  public static Builder builder() {
    return new Builder();
  }

  private final int bitmap;
  private final byte[] buffer = new byte[4];

  public ProtocolSteps(byte[] src, int offset) {
    if (offset + 4 > src.length) {
      throw new IllegalStateException("invalid supplied buffer length " + src.length
          + " for offset " + offset + " and data length 4 bytes");
    }

    System.arraycopy(src, offset, buffer, 0, 4);
    bitmap = ((buffer[0] & 0xFF) << 24)
        + ((buffer[1] & 0xFF) << 16)
        + ((buffer[2] & 0xFF) <<  8)
        + ((buffer[3] & 0xFF) <<  0);
  }

  private ProtocolSteps(Builder builder) {
    bitmap = builder.bitmap;
    buffer[0] = (byte) (bitmap >>> 24);
    buffer[1] = (byte) (bitmap >>> 16);
    buffer[2] = (byte) (bitmap >>>  8);
    buffer[3] = (byte) (bitmap >>>  0);
  }

  public byte[] array() {
    byte[] buffer = new byte[4];
    System.arraycopy(this.buffer, 0, buffer, 0, this.buffer.length);
    return buffer;
  }

  public int bitmap() {
    return bitmap;
  }

  /**
   * {@inheritDoc}
   */
  @Override public String toString() {
    return "ProtocolSteps: " + HEX.fromByteArray(buffer);
  }

  /**
   * {@inheritDoc}
   */
  @Override public int hashCode() {
    return 31 * 17 + bitmap;
  }

  /**
   * {@inheritDoc}
   */
  @Override public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ProtocolSteps)) {
      return false;
    }
    ProtocolSteps other = (ProtocolSteps) obj;
    return bitmap == other.bitmap;
  }
}
