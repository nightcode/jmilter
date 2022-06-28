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

public enum ByteArrays {
  ;

  public static byte[] intToByteArray(int src) {
    byte[] dst = new byte[4];
    dst[0] = (byte) (src >>> 24);
    dst[1] = (byte) (src >>> 16);
    dst[2] = (byte) (src >>>  8);
    dst[3] = (byte) (src >>>  0);
    return dst;
  }

  public static void intToByteArray(int src, byte[] dst) {
    if (dst.length < 4) {
      throw new IllegalArgumentException("invalid supplied buffer length " + dst.length + " for data length 4 bytes");
    }
    dst[0] = (byte) (src >>> 24);
    dst[1] = (byte) (src >>> 16);
    dst[2] = (byte) (src >>>  8);
    dst[3] = (byte) (src >>>  0);
  }

  public static void intToByteArray(int src, byte[] dst, int offset) {
    if (offset + 4 > dst.length) {
      throw new IllegalArgumentException("invalid supplied buffer length " + dst.length
          + " for offset " + offset + " and data length 4 bytes");
    }
    dst[offset + 0] = (byte) (src >>> 24);
    dst[offset + 1] = (byte) (src >>> 16);
    dst[offset + 2] = (byte) (src >>>  8);
    dst[offset + 3] = (byte) (src >>>  0);
  }
}
