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

package org.nightcode.milter;

import org.nightcode.common.base.Hexs;
import org.nightcode.milter.net.MilterPacket;
import org.nightcode.milter.util.ProtocolSteps;

import java.nio.charset.StandardCharsets;

import org.easymock.EasyMock;
import org.junit.Test;

public class MilterModificationServiceTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testAddHeader() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket('h'
        , HEX.toByteArray("444b494d2d46696c74657200204f70656e444b494d2046696c7465722076322e31312e"
        + "30206d782e6578616d706c652e6f7267204230394245353800"));

    EasyMock.expect(contextMock.getSessionProtocolSteps())
        .andReturn(ProtocolSteps.builder().headerValueLeadingSpace().build());

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.addHeader(contextMock, "DKIM-Filter", "OpenDKIM Filter v2.11.0 mx.example.org B09BE58");

    EasyMock.verify(contextMock);
  }

  @Test public void testChangeHeader() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket('m'
        , HEX.toByteArray("00000005444b494d2d46696c74657200204f70656e444b494d2046696c7465722076322e31312e"
        + "30206d782e6578616d706c652e6f7267204230394245353800"));

    EasyMock.expect(contextMock.getSessionProtocolSteps())
        .andReturn(ProtocolSteps.builder().headerValueLeadingSpace().build());

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.changeHeader(contextMock, 5,"DKIM-Filter", "OpenDKIM Filter v2.11.0 mx.example.org B09BE58");

    EasyMock.verify(contextMock);
  }

  @Test public void testInsertHeader() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket('i'
        , HEX.toByteArray("00000001444b494d2d46696c74657200204f70656e444b494d2046696c7465722076322e31312e" 
        + "30206d782e6578616d706c652e6f7267204230394245353800"));

    EasyMock.expect(contextMock.getSessionProtocolSteps())
        .andReturn(ProtocolSteps.builder().headerValueLeadingSpace().build());

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.insertHeader(contextMock, 1, "DKIM-Filter", "OpenDKIM Filter v2.11.0 mx.example.org B09BE58");

    EasyMock.verify(contextMock);
  }

  @Test public void testChangeFrom() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket('e'
        , HEX.toByteArray("737570706f7274406578616d706c652e6f7267006172677300"));

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.changeFrom(contextMock, "support@example.org", "args");

    EasyMock.verify(contextMock);
  }

  @Test public void testAddRecipient() throws MilterException {
    // TODO
  }

  @Test public void testAddRecipientEsmtpPar() throws MilterException {
    // TODO
  }

  @Test public void testDeleteRecipient() throws MilterException {
    // TODO
  }

  @Test public void testReplaceBody() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket('b'
        , HEX.toByteArray("6e657720626f64790d0a"));

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.replaceBody(contextMock, "new body\r\n".getBytes(StandardCharsets.UTF_8));

    EasyMock.verify(contextMock);
  }

  @Test public void testProgress() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket('p');

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.progress(contextMock);

    EasyMock.verify(contextMock);
  }

  @Test public void testQuarantine() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);
    
    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket('q', HEX.toByteArray("7465737400"));
    
    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();
    
    EasyMock.replay(contextMock);

    service.quarantine(contextMock, "test");

    EasyMock.verify(contextMock);
  }
}
