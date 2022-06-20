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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class MilterPacketUtilTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testSplitByZeroTermWithOffset() {
    byte[] buffer = HEX.toByteArray(
        "436a006d7830312e6578616d706c652e6f7267007b6461656d6f6e5f6e616d657d" 
        + "006d7830312e6578616d706c652e6f7267007600506f737466697820322e31302e3100" 
    );

    List<String> target = MilterPacketUtil.splitByZeroTerm(buffer, 1);
    Assert.assertEquals(6, target.size());
    Assert.assertEquals("j", target.get(0));
    Assert.assertEquals("mx01.example.org", target.get(1));
    Assert.assertEquals("Postfix 2.10.1", target.get(5));

  }
}
