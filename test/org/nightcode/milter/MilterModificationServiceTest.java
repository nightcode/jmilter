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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Hexs;

import org.junit.Assert;
import org.junit.Test;
import org.easymock.Capture;
import org.easymock.EasyMock;

import static org.nightcode.milter.ResponseCode.SMFIR_ADDHEADER;
import static org.nightcode.milter.ResponseCode.SMFIR_ADDRCPT;
import static org.nightcode.milter.ResponseCode.SMFIR_ADDRCPT_PAR;
import static org.nightcode.milter.ResponseCode.SMFIR_CHGFROM;
import static org.nightcode.milter.ResponseCode.SMFIR_CHGHEADER;
import static org.nightcode.milter.ResponseCode.SMFIR_DELRCPT;
import static org.nightcode.milter.ResponseCode.SMFIR_INSHEADER;
import static org.nightcode.milter.ResponseCode.SMFIR_PROGRESS;
import static org.nightcode.milter.ResponseCode.SMFIR_QUARANTINE;
import static org.nightcode.milter.ResponseCode.SMFIR_REPLBODY;
import static org.nightcode.milter.util.MilterPackets.MILTER_CHUNK_SIZE;

public class MilterModificationServiceTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testAddHeader() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket(SMFIR_ADDHEADER
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

    MilterPacket packet = new MilterPacket(SMFIR_CHGHEADER
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

    MilterPacket packet = new MilterPacket(SMFIR_INSHEADER
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

    MilterPacket packet = new MilterPacket(SMFIR_CHGFROM
        , HEX.toByteArray("737570706f7274406578616d706c652e6f7267006172677300"));

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.changeFrom(contextMock, "support@example.org", "args");

    EasyMock.verify(contextMock);
  }

  @Test public void testAddRecipient() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket(SMFIR_ADDRCPT
        , HEX.toByteArray("737570706f7274406578616d706c652e6f726700"));

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.addRecipient(contextMock, "support@example.org");

    EasyMock.verify(contextMock);
  }

  @Test public void testAddRecipientEsmtpPar() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket(SMFIR_ADDRCPT_PAR
        , HEX.toByteArray("737570706f7274406578616d706c652e6f7267006172677300"));

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.addRecipientEsmtpPar(contextMock, "support@example.org", "args");

    EasyMock.verify(contextMock);
  }

  @Test public void testDeleteRecipient() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket(SMFIR_DELRCPT
        , HEX.toByteArray("737570706f7274406578616d706c652e6f726700"));

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.deleteRecipient(contextMock, "support@example.org");

    EasyMock.verify(contextMock);
  }

  @Test public void testReplaceBody() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket(SMFIR_REPLBODY
        , HEX.toByteArray("6e657720626f64790d0a"));

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.replaceBody(contextMock, "new body\r\n".getBytes(StandardCharsets.UTF_8));

    EasyMock.verify(contextMock);
  }

  @Test public void testReplaceLargeBody() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    Capture<MilterPacket> packet1 = EasyMock.newCapture();
    Capture<MilterPacket> packet2 = EasyMock.newCapture();
    Capture<MilterPacket> packet3 = EasyMock.newCapture();
    
    contextMock.sendPacket(EasyMock.capture(packet1));
    EasyMock.expectLastCall().once();
    contextMock.sendPacket(EasyMock.capture(packet2));
    EasyMock.expectLastCall().once();
    contextMock.sendPacket(EasyMock.capture(packet3));
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    byte[] newBody = new byte[MILTER_CHUNK_SIZE * 2 + 5];
    ThreadLocalRandom.current().nextBytes(newBody);
    newBody[newBody.length - 2] = 0x0A;
    newBody[newBody.length - 1] = 0x0D;
    
    service.replaceBody(contextMock, newBody);

    EasyMock.verify(contextMock);

    Assert.assertEquals('b', packet1.getValue().command());
    Assert.assertArrayEquals(Arrays.copyOfRange(newBody, 0, MILTER_CHUNK_SIZE), packet1.getValue().payload());
    Assert.assertEquals('b', packet2.getValue().command());
    Assert.assertArrayEquals(Arrays.copyOfRange(newBody, MILTER_CHUNK_SIZE, MILTER_CHUNK_SIZE * 2), packet2.getValue().payload());
    Assert.assertEquals('b', packet3.getValue().command());
    Assert.assertArrayEquals(Arrays.copyOfRange(newBody, MILTER_CHUNK_SIZE * 2, newBody.length), packet3.getValue().payload());
  }

  @Test public void testProgress() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket(SMFIR_PROGRESS);

    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    service.progress(contextMock);

    EasyMock.verify(contextMock);
  }

  @Test public void testQuarantine() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);
    
    MessageModificationService service = new MessageModificationServiceImpl();

    MilterPacket packet = new MilterPacket(SMFIR_QUARANTINE, HEX.toByteArray("7465737400"));
    
    contextMock.sendPacket(packet);
    EasyMock.expectLastCall().once();
    
    EasyMock.replay(contextMock);

    service.quarantine(contextMock, "test");

    EasyMock.verify(contextMock);
  }
}
