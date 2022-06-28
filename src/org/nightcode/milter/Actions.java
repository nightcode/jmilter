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

import org.nightcode.milter.util.Hexs;

/**
 * Actions.
 */
public final class Actions {

  private static final Hexs HEX = Hexs.hex();

  /**
   * Builder for {@link Actions} instance.
   */
  public static final class Builder {
    private int bitmap = 0x00000000; // no flags

    private Builder() {
      // do nothing
    }

    /**
     * Creates an {@link Actions} instance.
     *
     * @return an {@link Actions} instance.
     */
    public Actions build() {
      return new Actions(this);
    }

    /**
     * Filter may add headers.
     *
     * @return the current {@link Builder} instance
     */
    public Builder addHeader() {
      bitmap |= 0x00000001;
      return this;
    }

    /**
     * Filter may replace body.
     *
     * @return the current {@link Builder} instance
     */
    public Builder replaceBody() {
      bitmap |= 0x00000002;
      return this;
    }

    /**
     * Filter may add recipients.
     *
     * @return the current {@link Builder} instance
     */
    public Builder addRecipients() {
      bitmap |= 0x00000004;
      return this;
    }

    /**
     * Filter may delete recipients.
     *
     * @return the current {@link Builder} instance
     */
    public Builder deleteRecipients() {
      bitmap |= 0x00000008;
      return this;
    }

    /**
     * Filter may change/delete headers.
     *
     * @return the current {@link Builder} instance
     */
    public Builder changeDeleteHeaders() {
      bitmap |= 0x00000010;
      return this;
    }

    /**
     * Filter may quarantine envelope.
     *
     * @return the current {@link Builder} instance
     */
    public Builder quarantineEnvelope() {
      bitmap |= 0x00000020;
      return this;
    }

    /**
     * Filter may change "from" (envelope sender).
     *
     * @return the current {@link Builder} instance
     */
    public Builder changeFrom() {
      bitmap |= 0x00000040;
      return this;
    }

    /**
     * Filter may add recipients including arguments.
     *
     * @return the current {@link Builder} instance
     */
    public Builder addRecipientsInclArgs() {
      bitmap |= 0x00000080;
      return this;
    }

    /**
     * Filter can send set of symbols (macros) that it wants.
     *
     * @return the current {@link Builder} instance
     */
    public Builder setSymList() {
      bitmap |= 0x00000100;
      return this;
    }
  }

  // The actions of V1 filter
  public static final Actions DEF_ACTIONS = Actions.builder()
      .addHeader()
      .replaceBody()
      .addRecipients()
      .deleteRecipients()
      .build();

  public static Builder builder() {
    return new Builder();
  }

  private final int bitmap;
  private final byte[] buffer = new byte[4];

  public Actions(byte[] src, int offset) {
    if (offset < 0 || offset + 4 > src.length) {
      throw new IllegalArgumentException("invalid supplied buffer length " + src.length
          + " for offset " + offset + " and data length 4 bytes");
    }

    System.arraycopy(src, offset, this.buffer, 0, 4);
    bitmap = ((buffer[0] & 0xFF) << 24)
        + ((buffer[1] & 0xFF) << 16)
        + ((buffer[2] & 0xFF) <<  8)
        + ((buffer[3] & 0xFF) <<  0);
  }

  private Actions(Builder builder) {
    bitmap = builder.bitmap;
    buffer[0] = (byte) (bitmap >>> 24);
    buffer[1] = (byte) (bitmap >>> 16);
    buffer[2] = (byte) (bitmap >>>  8);
    buffer[3] = (byte) (bitmap >>>  0);
  }

  /**
   * Returns actions buffer.
   *
   * @return actions buffer
   */
  public byte[] array() {
    byte[] buffer = new byte[4];
    System.arraycopy(this.buffer, 0, buffer, 0, this.buffer.length);
    return buffer;
  }

  public int bitmap() {
    return bitmap;
  }

  public void writeTo(byte[] dst, int offset) {
    if (offset < 0 || offset + 4 > dst.length) {
      throw new IllegalArgumentException("invalid supplied buffer length " + dst.length
          + " for offset " + offset + " and data length 4 bytes");
    }
    System.arraycopy(buffer, 0, dst, offset, buffer.length);
  }

  /**
   * {@inheritDoc}
   */
  @Override public String toString() {
    return "Actions: " + HEX.fromByteArray(buffer);
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
    if (!(obj instanceof Actions)) {
      return false;
    }
    Actions other = (Actions) obj;
    return bitmap == other.bitmap;
  }
}
