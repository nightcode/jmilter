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

public enum ProtocolFamily implements Code {

  SMFIA_UNKNOWN('U'), // Unknown (NOTE: Omits "port" and "host" fields entirely)
  SMFIA_UNIX   ('L'), // Unix (AF_UNIX/AF_LOCAL) socket ("port" is 0)
  SMFIA_INET   ('4'), // TCPv4 connection
  SMFIA_INET6  ('6'); // TCPv6 connection

  private final int code;

  ProtocolFamily(int code) {
    this.code = code;
  }

  @Override public int code() {
    return code;
  }
}
