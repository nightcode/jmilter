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

import org.junit.Assert;
import org.junit.Test;

public class ProtocolStepsTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testNewInstance() {
    byte[] payload = HEX.toByteArray("001fffff");

    ProtocolSteps protocolSteps = new ProtocolSteps(payload, 0);
    Assert.assertEquals(0x001FFFFF, protocolSteps.bitmap());
    Assert.assertArrayEquals(HEX.toByteArray("001fffff"), protocolSteps.array());

    payload = HEX.toByteArray("00000006000001ff001fffff");

    protocolSteps = new ProtocolSteps(payload, 8);
    Assert.assertEquals(0x001FFFFF, protocolSteps.bitmap());
    Assert.assertArrayEquals(HEX.toByteArray("001fffff"), protocolSteps.array());
  }
}
