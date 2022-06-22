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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public enum NetUtils {
  ;

  public static void checkPort(int port) {
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("illegal port value: " + port);
    }
  }

  public static InetSocketAddress parseAddress(String address) {
    Objects.requireNonNull(address, "address");

    String host;
    int port;
    if (address.charAt(0) == '[') {
      int colonIndex = address.indexOf(':');
      int closeBracketIndex = address.lastIndexOf(']');
      if (colonIndex < 0 || closeBracketIndex < colonIndex) {
        throw new IllegalArgumentException("illegal address: " + address);
      }

      host = address.substring(1, closeBracketIndex);
      if (closeBracketIndex + 1 == address.length() || address.charAt(closeBracketIndex + 1) != ':') {
        throw new IllegalArgumentException("illegal port value in address: " + address);
      }

      port = Integer.parseInt(address.substring(closeBracketIndex + 2));
    } else {
      int colonIndex = address.indexOf(':');
      if (colonIndex < 0 || address.indexOf(':', colonIndex + 1) >= 0) {
        throw new IllegalArgumentException("illegal address: " + address);
      }

      host = address.substring(0, colonIndex);
      port = Integer.parseInt(address.substring(colonIndex + 1));
    }

    InetAddress inetAddress;
    try {
      inetAddress = InetAddress.getByName(host);
    } catch (UnknownHostException ex) {
      throw new IllegalArgumentException(ex);
    }
    checkPort(port);

    return new InetSocketAddress(inetAddress, port);
  }
}
