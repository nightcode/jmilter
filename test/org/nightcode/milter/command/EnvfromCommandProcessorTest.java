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

import java.util.ArrayList;
import java.util.List;

import org.nightcode.milter.MilterException;
import org.nightcode.milter.codec.MilterPacket;

import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_MAIL;

public class EnvfromCommandProcessorTest extends AbstractCommandProcessorTest {

  @Test public void testSubmit() throws MilterException {
    List<String> list = new ArrayList<>();
    list.add("<support@example.org>");
    list.add("SIZE=1552");
    list.add("BODY=8BITMIME");

    MilterPacket packet = new MilterPacket(SMFIC_MAIL
        , HEX.toByteArray("3c737570706f7274406578616d706c652e6f72673e" 
        + "0053495a453d3135353200424f44593d384249544d494d4500"));

    execute(packet, new EnvfromCommandProcessor(), ctx -> {
      try {
        ctx.handler().envfrom(ctx, list);
        EasyMock.expectLastCall().once();
      } catch (MilterException e) {
        throw new RuntimeException(e);
      }
    });
  }
}

