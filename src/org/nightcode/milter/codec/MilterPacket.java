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

package org.nightcode.milter.codec;

import java.util.Arrays;
import java.util.Objects;

import org.nightcode.milter.Code;
import org.nightcode.milter.util.Hexs;

/**
 * Milter packet.
 */
public final class MilterPacket {

  /**
   * Helper class for creating Milter packet.
   */
  public static final class Builder {
    private int command;
    private byte[] payload = ZERO_ARRAY;

    private Builder() {
      // do nothing
    }

    /**
     * Creates new Milter packet.
     *
     * @return a new Milter packet
     */
    public MilterPacket build() {
      return new MilterPacket(this);
    }

    /**
     * Sets Milter protocol command.
     *
     * @param val milter protocol command
     * @return current Builder instance
     */
    public Builder command(int val) {
      command = val;
      return this;
    }

    /**
     * Sets Milter protocol command.
     *
     * @param val milter command code
     * @return current Builder instance
     */
    public Builder command(Code val) {
      command = val.code();
      return this;
    }

    /**
     * Sets Milter protocol payload.
     *
     * @param val Milter protocol payload
     * @return current Builder instance
     */
    public Builder payload(byte[] val) {
      Objects.requireNonNull(val, "payload");
      byte[] buffer = new byte[val.length];
      System.arraycopy(val, 0, buffer, 0, val.length);
      payload = buffer;
      return this;
    }

    public Builder payload(byte[] val, int offset, int length) {
      Objects.requireNonNull(val, "payload");
      if (val.length < offset + length) {
        throw new IllegalArgumentException(String.format("buffer length %s less then offset %s plus length %s"
            , val.length, offset, length));
      }
      byte[] buffer = new byte[length];
      System.arraycopy(val, offset, buffer, 0, length);
      payload = buffer;
      return this;
    }
  }

  private static final Hexs HEX = Hexs.hex().withByteSeparator(" ");

  private static final byte[] ZERO_ARRAY = new byte[0];

  public static final int COMMAND_LENGTH = 1;

  /**
   * Creates new {@link Builder} instance.
   *
   * @return new {@link Builder} instance
   */
  public static Builder builder() {
    return new Builder();
  }

  private final int command;
  private final byte[] payload;

  /**
   * Creates a new Milter packet.
   *
   * @param commandCode Milter command code
   */
  public MilterPacket(Code commandCode) {
    this(commandCode, ZERO_ARRAY);
  }

  /**
   * Creates a new Milter packet.
   *
   * @param command Milter protocol command
   * @param payload Milter protocol payload
   */
  public MilterPacket(Code command, byte[] payload) {
    this(command.code(), payload, 0, payload.length);
  }

  /**
   * Creates a new Milter packet.
   *
   * @param command Milter protocol command
   */
  MilterPacket(int command) {
    this(command, ZERO_ARRAY);
  }

  /**
   * Creates a new Milter packet.
   *
   * @param command Milter protocol command
   * @param payload Milter protocol payload
   */
  MilterPacket(int command, byte[] payload) {
    this(command, payload, 0, payload.length);
  }

  /**
   * Creates a new Milter packet.
   *
   * @param command Milter protocol command
   * @param payload Milter protocol payload
   * @param offset offset in the supplied payload array
   * @param length length of Milter protocol payload
   */
  MilterPacket(int command, byte[] payload, int offset, int length) {
    this.command = command;
    if (payload.length != 0) {
      this.payload = new byte[length];
      System.arraycopy(payload, offset, this.payload, 0, length);
    } else {
      this.payload = ZERO_ARRAY;
    }
  }

  private MilterPacket(Builder builder) {
    command = builder.command;
    payload = builder.payload;
  }

  /**
   * Returns Milter protocol command.
   *
   * @return Milter protocol command
   */
  public int command() {
    return command;
  }

  /**
   * Returns Milter protocol payload.
   *
   * @return Milter protocol payload
   */
  public byte[] payload() {
    if (payload.length == 0) {
      return ZERO_ARRAY;
    }
    byte[] buffer = new byte[payload.length];
    System.arraycopy(payload, 0, buffer, 0, payload.length);
    return buffer;
  }

  @Override public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof MilterPacket)) {
      return false;
    }
    MilterPacket other = (MilterPacket) obj;
    return command == other.command && Arrays.equals(payload, other.payload);
  }

  @Override public int hashCode() {
    int result = 17;
    result = 31 * result + command;
    result = 31 * result + Arrays.hashCode(payload);
    return result;
  }

  @Override public String toString() {
    return "MilterPacket{"
        + "command=" + (char) command
        + ", payload=" + (payload.length > 0 ? HEX.fromByteArray(payload) : "EMPTY")
        + '}';
  }
}
