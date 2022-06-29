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

package org.nightcode.milter.client;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.nightcode.milter.Actions;
import org.nightcode.milter.CommandCode;
import org.nightcode.milter.ProtocolFamily;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.ByteArrays;

import static java.lang.String.format;
import static org.nightcode.milter.CommandCode.SMFIC_ABORT;
import static org.nightcode.milter.CommandCode.SMFIC_BODY;
import static org.nightcode.milter.CommandCode.SMFIC_CONNECT;
import static org.nightcode.milter.CommandCode.SMFIC_DATA;
import static org.nightcode.milter.CommandCode.SMFIC_EOB;
import static org.nightcode.milter.CommandCode.SMFIC_EOH;
import static org.nightcode.milter.CommandCode.SMFIC_HEADER;
import static org.nightcode.milter.CommandCode.SMFIC_HELO;
import static org.nightcode.milter.CommandCode.SMFIC_MACRO;
import static org.nightcode.milter.CommandCode.SMFIC_MAIL;
import static org.nightcode.milter.CommandCode.SMFIC_OPTNEG;
import static org.nightcode.milter.CommandCode.SMFIC_QUIT;
import static org.nightcode.milter.CommandCode.SMFIC_QUIT_NC;
import static org.nightcode.milter.CommandCode.SMFIC_RCPT;
import static org.nightcode.milter.util.MilterPackets.MILTER_CHUNK_SIZE;
import static org.nightcode.milter.util.MilterPackets.ZERO_TERM;
import static org.nightcode.milter.util.MilterPackets.ZERO_TERM_LENGTH;
import static org.nightcode.milter.util.MilterPackets.createZeroTerm;
import static org.nightcode.milter.util.MilterPackets.getLengthSafe;
import static org.nightcode.milter.util.MilterPackets.safeCopy;

public enum MilterPacketFactory {
  ;

  private static final int CONNECT_FAMILY_LENGTH = 1;
  private static final int CONNECT_PORT_LENGTH   = 2;

  public static MilterPacket createAbort() {
    return new MilterPacket(SMFIC_ABORT);
  }

  public static MilterPacket createBodyChunk(byte[] buffer) {
    if (buffer.length > MILTER_CHUNK_SIZE) {
      throw new IllegalArgumentException(format("body chunk size (%s) should be less than %s bytes", buffer.length, MILTER_CHUNK_SIZE));
    }
    return new MilterPacket(SMFIC_BODY, buffer);
  }

  public static MilterPacket createConnect(String hostname, ProtocolFamily family, int port, String address) {
    byte[] h = hostname.getBytes(StandardCharsets.UTF_8);
    byte[] a = address.getBytes(StandardCharsets.UTF_8);

    byte[] payload = new byte[h.length + ZERO_TERM_LENGTH + CONNECT_FAMILY_LENGTH + CONNECT_PORT_LENGTH + a.length + ZERO_TERM_LENGTH];

    int offset = 0;
    System.arraycopy(h, 0, payload, offset, h.length);
                        offset += h.length;
    payload[offset++] = ZERO_TERM;

    payload[offset++] = (byte) family.code();

    payload[offset++] = (byte) (port >>> 8);
    payload[offset++] = (byte) port;

    System.arraycopy(a, 0, payload, offset, a.length);
                      offset += a.length;
    payload[offset] = ZERO_TERM;

    return new MilterPacket(SMFIC_CONNECT, payload);
  }

  public static MilterPacket createData(byte[] buffer) {
    return new MilterPacket(SMFIC_DATA, buffer);
  }

  public static MilterPacket createEnvfrom(List<String> args) {
    byte[] payload = createZeroTerm(args);
    return new MilterPacket(SMFIC_MAIL, payload);
  }

  public static MilterPacket createEnvrcpt(List<String> args) {
    byte[] payload = createZeroTerm(args);
    return new MilterPacket(SMFIC_RCPT, payload);
  }

  public static MilterPacket createEob() {
    return new MilterPacket(SMFIC_EOB);
  }

  public static MilterPacket createEoh() {
    return new MilterPacket(SMFIC_EOH);
  }

  public static MilterPacket createHeader(String name, String value) {
    byte[] n = name.getBytes(StandardCharsets.UTF_8);
    byte[] v = value.getBytes(StandardCharsets.UTF_8);

    byte[] payload = new byte[n.length + ZERO_TERM_LENGTH + v.length + ZERO_TERM_LENGTH];

    int offset = 0;
    System.arraycopy(n, 0, payload, 0, n.length);
                        offset += n.length;
    payload[offset++] = ZERO_TERM;

    System.arraycopy(v, 0, payload, offset, v.length);
                      offset += v.length;
    payload[offset] = ZERO_TERM;

    return new MilterPacket(SMFIC_HEADER, payload);
  }

  public static MilterPacket createHelo(String helo) {
    byte[] payload = new byte[helo.length() + ZERO_TERM_LENGTH];

    byte[] h = helo.getBytes(StandardCharsets.UTF_8);
    System.arraycopy(h, 0, payload,  0, h.length);
    payload[h.length] = ZERO_TERM;

    return new MilterPacket(SMFIC_HELO, payload);
  }

  public static MilterPacket createMacro(CommandCode code, Macros macros) {
    byte[] payload = new byte[1 + zeroTermLength(macros)];

    payload[0] = (byte) code.code();
    writeZeroTerm(macros, payload, 1);

    return new MilterPacket(SMFIC_MACRO, payload);
  }

  public static MilterPacket createOptneg(int version, Actions actions, ProtocolSteps steps) {
    byte[] payload = new byte[12];

    ByteArrays.intToByteArray(version, payload);
    actions.writeTo(payload, 4);
    steps.writeTo(payload, 8);

    return new MilterPacket(SMFIC_OPTNEG, payload);
  }

  public static MilterPacket createQuit() {
    return new MilterPacket(SMFIC_QUIT);
  }

  public static MilterPacket createQuitNc() {
    return new MilterPacket(SMFIC_QUIT_NC);
  }

  private static int zeroTermLength(Macros args) {
    int length = 0;
    for (Macros.Pair pair : args.pairs()) {
      length += getLengthSafe(pair.key);
      length += ZERO_TERM_LENGTH;
      length += getLengthSafe(pair.value);
      length += ZERO_TERM_LENGTH;
    }
    return length;
  }

  private static void writeZeroTerm(Macros args, byte[] dst, int offset) {
    int length = zeroTermLength(args);
    if (offset + length > dst.length) {
      throw new IllegalArgumentException(format("invalid supplied buffer length %s for offset %s and data length %s bytes"
          , dst.length, offset, length));
    }
    for (Macros.Pair pair : args.pairs()) {
      offset = safeCopy(pair.key, dst, offset);
      offset = safeCopy(pair.value, dst, offset);
    }
  }
}
