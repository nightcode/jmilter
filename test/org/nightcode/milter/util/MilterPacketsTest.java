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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class MilterPacketsTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testSplitByZeroTermWithOffset() {
    byte[] buffer = HEX.toByteArray(
        "436a006d7830312e6578616d706c652e6f7267007b6461656d6f6e5f6e616d657d" 
        + "006d7830312e6578616d706c652e6f7267007600506f737466697820322e31302e3100" 
    );

    List<String> target = MilterPackets.splitByZeroTerm(buffer, 1);
    Assert.assertEquals(6, target.size());
    Assert.assertEquals("j", target.get(0));
    Assert.assertEquals("mx01.example.org", target.get(1));
    Assert.assertEquals("Postfix 2.10.1", target.get(5));
  }

  @Test public void testIndexOfZeroTerm() {
    Assert.assertThrows(IllegalArgumentException.class, () -> MilterPackets.indexOfZeroTerm(new byte[0], 5));
    Assert.assertThrows(IllegalArgumentException.class, () -> MilterPackets.indexOfZeroTerm(new byte[0], -1));
  }
  
  @Test public void testSplitByZeroTerm() {
    Assert.assertThrows(IllegalArgumentException.class, () -> MilterPackets.splitByZeroTerm(new byte[0], 5));
    Assert.assertThrows(IllegalArgumentException.class, () -> MilterPackets.splitByZeroTerm(new byte[0], -1));
  }
  
  @Test public void testGetLengthSafe() {
    Assert.assertEquals(0, MilterPackets.getLengthSafe(null));
  }

  @Test public void testSafeCopy() {
    Assert.assertEquals(7, MilterPackets.safeCopy(null, new byte[0], 7));
    Assert.assertEquals(7, MilterPackets.safeCopy("", new byte[0], 7));
  }
}
