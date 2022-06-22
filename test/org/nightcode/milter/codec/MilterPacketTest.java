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

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class MilterPacketTest {

  @Test public void testHashCode() {
    Random random = new Random(0);

    int command = random.nextInt();
    byte[] payload = new byte[random.nextInt(128)];
    
    MilterPacket packet0 = new MilterPacket(command, payload);
    MilterPacket packet1 = new MilterPacket(command, payload);

    Assert.assertEquals(packet0.hashCode(), packet1.hashCode());
  }
}
