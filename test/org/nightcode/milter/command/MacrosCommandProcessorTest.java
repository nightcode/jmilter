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

package org.nightcode.milter.command;

import java.util.Map;

import org.nightcode.milter.MilterException;
import org.nightcode.milter.codec.MilterPacket;

import org.junit.Assert;
import org.junit.Test;
import org.easymock.Capture;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_CONNECT;
import static org.nightcode.milter.CommandCode.SMFIC_MACRO;

public class MacrosCommandProcessorTest extends AbstractCommandProcessorTest {

  @Test public void testSubmit() throws MilterException {
    MilterPacket packet = MilterPacket.builder()
        .command(SMFIC_MACRO)
        .payload(HEX.toByteArray("436a006d782e6578616d706c652e6f7267007b6461656d6f6e5f6e616d657d00" 
            + "6d782e6578616d706c652e6f7267007600506f737466697820322e31302e3100"))
        .build();

    Capture<Map<String, String>> mapCapture = EasyMock.newCapture();

    execute(packet, new MacrosCommandProcessor(), ctx -> {
      try {
        ctx.handler().macro(EasyMock.eq(ctx), EasyMock.eq(SMFIC_CONNECT.code()), EasyMock.capture(mapCapture));
        EasyMock.expectLastCall().once();
      } catch (MilterException e) {
        throw new RuntimeException(e);
      }
    });

    Map<String, String> target = mapCapture.getValue();
    Assert.assertEquals(3, target.size());
    Assert.assertEquals("mx.example.org", target.get("j"));
    Assert.assertEquals("mx.example.org", target.get("{daemon_name}"));
    Assert.assertEquals("Postfix 2.10.1", target.get("v"));
  }
}
