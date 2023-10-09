package org.nightcode.milter;

import java.nio.charset.StandardCharsets;

import org.nightcode.milter.util.ByteArrays;

public final class MilterMacros {

  public static final class Builder {
    private final String[] list = new String[MacroPlace.MAX_MACROS_ENTRIES];

    private Builder() {
      // do nothing
    }

    public MilterMacros build() {
      return new MilterMacros(this);
    }

    public Builder connectMacros(String val) {
      setMacros(MacroPlace.SMFIM_CONNECT, val);
      return this;
    }

    public Builder heloMacros(String val) {
      setMacros(MacroPlace.SMFIM_HELO, val);
      return this;
    }

    public Builder envfromMacros(String val) {
      setMacros(MacroPlace.SMFIM_ENVFROM, val);
      return this;
    }

    public Builder envrcptMarcos(String val) {
      setMacros(MacroPlace.SMFIM_ENVRCPT, val);
      return this;
    }

    public Builder dataMacros(String val) {
      setMacros(MacroPlace.SMFIM_DATA, val);
      return this;
    }

    public Builder eohMacros(String val) {
      setMacros(MacroPlace.SMFIM_EOH, val);
      return this;
    }

    public Builder eomMacros(String val) {
      setMacros(MacroPlace.SMFIM_EOM, val);
      return this;
    }

    private void setMacros(MacroPlace place,  String value) {
      list[place.index()] = value;
    }
  }

  private static final byte[] EMPTY_ARRAY = new byte[0];

  private static final String[] EMPTY_MILTER_MACROS_LIST = new String[MacroPlace.MAX_MACROS_ENTRIES];

  private static final MilterMacros INSTANCE = new MilterMacros();

  public static Builder builder() {
    return new Builder();
  }

  public static MilterMacros instance() {
    return INSTANCE;
  }

  private final String[] macrosList;

  private MilterMacros() {
    macrosList = EMPTY_MILTER_MACROS_LIST;
  }

  private MilterMacros(Builder builder) {
    macrosList = builder.list;
  }

  public byte[] array() {
    int length = 0;
    for (int i = 0; i < MacroPlace.MAX_MACROS_ENTRIES; i++) {
      if (macrosList[i] != null) {
        length += (4 + macrosList[i].length() + 1);
      }
    }

    if (length == 0) {
      return EMPTY_ARRAY;
    }

    byte[] buffer = new byte[length];
    int offset = 0;
    for (int i = 0; i < MacroPlace.MAX_MACROS_ENTRIES; i++) {
      if (macrosList[i] != null) {
        ByteArrays.intToByteArray(i, buffer, offset);
        offset += 4;
        byte[] list = macrosList[i].getBytes(StandardCharsets.UTF_8);
        System.arraycopy(list, 0, buffer, offset, list.length);
        offset += (list.length + 1);
      }
    }

    return buffer;
  }
}
