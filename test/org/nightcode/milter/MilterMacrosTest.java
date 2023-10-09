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

package org.nightcode.milter;

import org.nightcode.milter.util.Hexs;

import org.junit.Assert;
import org.junit.Test;

public class MilterMacrosTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testNewInstance() {
    MilterMacros milterMacros = MilterMacros.builder()
        .envfromMacros("")
        .envrcptMarcos("{rcpt_addr}")
        .build();

    System.out.println(HEX.fromByteArray(milterMacros.array()));
    
    Assert.assertArrayEquals(HEX.toByteArray("0000000200000000037B726370745F616464727D00"), milterMacros.array());
  }
}