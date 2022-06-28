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

import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Test;

public class NetUtilsTest {

  @Test public void testCheckPort() {
    NetUtils.checkPort(25);

    Assert.assertThrows(IllegalArgumentException.class, () -> NetUtils.checkPort(-1));
    Assert.assertThrows(IllegalArgumentException.class, () -> NetUtils.checkPort(65536));
  }

  @Test public void testParseAddress() {
    InetSocketAddress expectedIpV4 = new InetSocketAddress("127.0.0.1", 458);
    InetSocketAddress target       = NetUtils.parseAddress("127.0.0.1:458");
    Assert.assertEquals(expectedIpV4, target);

    InetSocketAddress expectedIpV6 = new InetSocketAddress("2001:db8:85a3:8d3:1319:8a2e:370:7348", 458);
    target = NetUtils.parseAddress("[2001:db8:85a3:8d3:1319:8a2e:370:7348]:458");
    Assert.assertEquals(expectedIpV6, target);
  }
}
