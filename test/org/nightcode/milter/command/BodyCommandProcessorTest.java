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

package org.nightcode.milter.command;

import java.nio.charset.StandardCharsets;

import org.nightcode.milter.MilterException;
import org.nightcode.milter.codec.MilterPacket;

import org.junit.Test;

import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_BODY;

public class BodyCommandProcessorTest extends AbstractCommandProcessorTest {

  @Test public void testSubmit() throws MilterException {
    String       bodyText = "test data\r\n";
    MilterPacket packet   = new MilterPacket(SMFIC_BODY, bodyText.getBytes(StandardCharsets.UTF_8));

    execute(packet, new BodyCommandProcessor(), ctx -> {
      try {
        ctx.handler().body(ctx, bodyText);
        EasyMock.expectLastCall().once();
      } catch (MilterException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
