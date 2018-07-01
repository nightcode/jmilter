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

import org.nightcode.common.base.Hexs;

import org.junit.Assert;
import org.junit.Test;

public class ActionsTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testNewInstanceFromByteArrayWithOffset() {
    byte[] payload = HEX.toByteArray("000001ff");

    Actions actions = new Actions(payload, 0);
    Assert.assertArrayEquals(HEX.toByteArray("000001ff"), actions.array());

    payload = HEX.toByteArray("00000006000001ff001fffff");

    actions = new Actions(payload, 4);
    Assert.assertArrayEquals(HEX.toByteArray("000001ff"), actions.array());
  }
}
