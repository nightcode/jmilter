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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static org.nightcode.milter.util.Properties.OptionValue.Type.BOOLEAN;
import static org.nightcode.milter.util.Properties.OptionValue.Type.BYTE;
import static org.nightcode.milter.util.Properties.OptionValue.Type.INT;
import static org.nightcode.milter.util.Properties.OptionValue.Type.LONG;
import static org.nightcode.milter.util.Properties.OptionValue.Type.STRING;

public enum Properties {
  ;

  static final class OptionValue {
    enum Type {
      BOOLEAN(0x00000001),
      BYTE   (0x00000002),
      INT    (0x00000004),
      LONG   (0x00000008),
      STRING (0x00000010);

      private final int bitField;

      Type(int bitField) {
        this.bitField = bitField;
      }
    }

    private int bitField;

    private boolean booleanValue;
    private byte    byteValue;
    private int     intValue;
    private long    longValue;
    private String  stringValue;

    private OptionValue() {
      // do nothing
    }

    boolean getBoolean() {
      return booleanValue;
    }

    byte getByte() {
      return byteValue;
    }

    int getInt() {
      return intValue;
    }

    long getLong() {
      return longValue;
    }

    String getString() {
      return stringValue;
    }

    boolean has(Type type) {
      return (bitField & type.bitField) == type.bitField;
    }

    private OptionValue setBoolean(boolean value) {
      bitField |= BOOLEAN.bitField;
      booleanValue = value;
      return this;
    }

    private OptionValue setByte(byte value) {
      bitField |= BYTE.bitField;
      byteValue = value;
      return this;
    }

    private OptionValue setInt(int value) {
      bitField |= INT.bitField;
      intValue = value;
      return this;
    }

    private OptionValue setLong(long value) {
      bitField |= LONG.bitField;
      longValue = value;
      return this;
    }

    private OptionValue setString(String value) {
      bitField |= STRING.bitField;
      stringValue = value;
      return this;
    }
  }

  private static final ConcurrentMap<ConfigOption, OptionValue> OPTIONS = new ConcurrentHashMap<>();

  public static boolean getBoolean(ConfigOption option, boolean def) {
    OptionValue value = get(option, BOOLEAN, s -> new OptionValue().setBoolean((s == null) ? def : Boolean.parseBoolean(s)));
    return value.getBoolean();
  }

  public static byte getByte(ConfigOption option, byte def) {
    OptionValue value = get(option, BOOLEAN, s -> new OptionValue().setByte((s == null) ? def : Byte.parseByte(s)));
    return value.getByte();
  }

  public static int getInt(ConfigOption option, int def) {
    OptionValue value = get(option, INT, s -> new OptionValue().setInt((s == null) ? def : Integer.parseInt(s)));
    return value.getInt();
  }

  public static long getLong(ConfigOption option, long def) {
    OptionValue value = get(option, LONG, s -> new OptionValue().setLong((s == null) ? def : Long.parseLong(s)));
    return value.getLong();
  }

  public static String getString(ConfigOption option, String def) {
    OptionValue value = get(option, STRING, s -> new OptionValue().setString(s == null ? def : s));
    return value.getString();
  }

  private static OptionValue get(ConfigOption option, OptionValue.Type type, Function<String, ? extends OptionValue> parser) {
    OptionValue value = OPTIONS.computeIfAbsent(option, o -> {
      String val = System.getProperty(o.key());
      return parser.apply(val);
    });
    if (!value.has(type)) {
      throw new IllegalStateException("unable to get <" + option + "> of type " + type);
    }
    return value;
  }
}
