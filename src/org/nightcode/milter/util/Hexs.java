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

package org.nightcode.milter.util;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

/**
 * An object which convert hexadecimal string to byte array and vice versa.
 */
public final class Hexs {

  public static void validArgument(boolean expression, String message, Object... messageArgs) {
    if (!expression) {
      throw new IllegalArgumentException(format(message, messageArgs));
    }
  }

  private static final char[] LOWER_HEX_DIGITS = "0123456789abcdef".toCharArray();
  private static final char[] UPPER_HEX_DIGITS = "0123456789ABCDEF".toCharArray();

  private final String byteSeparator;
  private final char[] hexDigits;
  private final boolean lowerCase;

  /**
   * Returns a Hexs that uses upper case hex digits
   * and empty string as byte separator.
   *
   * @return a Hexs that uses upper case hex digits
   *         and empty string as byte separator
   */
  public static Hexs hex() {
    return new Hexs();
  }

  private Hexs() {
    this(null, false);
  }

  private Hexs(@Nullable String byteSeparator, boolean lowerCase) {
    this.byteSeparator = byteSeparator;
    this.lowerCase = lowerCase;
    if (lowerCase) {
      hexDigits = LOWER_HEX_DIGITS;
    } else {
      hexDigits = UPPER_HEX_DIGITS;
    }
  }

  /**
   * Returns a hexadecimal string representation of each bytes of {@code bytes}.
   *
   * @param bytes a bytes to convert
   * @return a hexadecimal string representation of each bytes of {@code bytes}
   */
  public String fromByteArray(byte[] bytes) {
    java.util.Objects.requireNonNull(bytes, "bytes");
    return fromByteArray(bytes, 0, bytes.length);
  }

  /**
   * Returns a hexadecimal string representation of {@code length} bytes of {@code bytes}
   * starting at offset {@code offset}.
   *
   * @param bytes a bytes to convert
   * @param offset start offset in the bytes
   * @param length maximum number of bytes to use
   * @return a hexadecimal string representation of each bytes of {@code bytes}
   */
  public String fromByteArray(byte[] bytes, int offset, int length) {
    Objects.requireNonNull(bytes, "bytes");
    validArgument(offset >= 0, "offset must be equal or greater than zero");
    validArgument(length >= 0, "length must be greater than zero");
    validArgument(offset + length <= bytes.length, "(offset + length) must be less than %s", bytes.length);
    if (byteSeparator != null && byteSeparator.length() > 0) {
      return fromByteArrayInternal(bytes, offset, length, byteSeparator);
    } else {
      return fromByteArrayInternal(bytes, offset, length);
    }
  }

  /**
   * Returns a byte array representation of hexadecimal string {@code hexString}.
   *
   * @param hexString  hexadecimal string to convert
   * @return a byte array representation of hexadecimal string {@code hexString}
   */
  public byte[] toByteArray(String hexString) {
    Objects.requireNonNull(hexString, "hexadecimal string");
    validArgument((hexString.length() & 0x1) == 0
        , "hexadecimal string <%s> must have an even number of characters.", hexString);
    int length = hexString.length();
    byte[] result = new byte[length >> 1];
    for (int i = 0; i < length; i += 2) {
      int hn = Character.digit(hexString.charAt(i), 16);
      int ln = Character.digit(hexString.charAt(i + 1), 16);
      result[i >> 1] = (byte) ((hn << 4) | ln);
    }
    return result;
  }

  /**
   * Returns a Hexs that uses lower case hex digits,
   * and the same configuration as this Hexs otherwise.
   *
   * @return a Hexs that uses lower case hex digits
   */
  public Hexs lowerCase() {
    return new Hexs(byteSeparator, true);
  }

  /**
   * Returns a Hexs using the given byte separator,
   * and the same configuration as this Hexs otherwise.
   *
   * @param byteSeparator byte separator
   * @return a Hexs using the given byte separator
   */
  public Hexs withByteSeparator(String byteSeparator) {
    return new Hexs(byteSeparator, lowerCase);
  }

  private String fromByteArrayInternal(byte[] bytes, int offset, int length) {
    int capacity = length << 1;
    StringBuilder builder = new StringBuilder(capacity);
    int size = offset + length;
    for (int i = offset; i < size; i++) {
      builder.append(hexDigits[(bytes[i] & 0xF0) >> 4]);
      builder.append(hexDigits[bytes[i] & 0x0F]);
    }
    return builder.toString();
  }

  private String fromByteArrayInternal(byte[] bytes, int offset, int length, String byteSeparator) {
    int capacity = (length << 1) + (length - 1) * byteSeparator.length();
    StringBuilder builder = new StringBuilder(capacity);
    int size = offset + length;
    for (int i = offset; i < size; i++) {
      builder.append(hexDigits[(bytes[i] & 0xF0) >> 4]);
      builder.append(hexDigits[bytes[i] & 0x0F]);
      if (i < bytes.length - 1) {
        builder.append(byteSeparator);
      }
    }
    return builder.toString();
  }
}
