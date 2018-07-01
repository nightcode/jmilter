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

import org.nightcode.common.base.Hexs;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.MilterState;
import org.nightcode.milter.net.MilterPacket;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Test;

public class CommandEngineTest {

  private static final Hexs HEX = Hexs.hex();

  private final UUID contextId = UUID.randomUUID();

  @Test public void testSubmit() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = MilterPacket.builder()
        .command((byte) 0x00)
        .build();

    milterContextMock.setSessionState(MilterState.UNKNOWN);
    EasyMock.expectLastCall().once();
    
    milterHandlerMock.unknown(milterContextMock, packet.payload());
    EasyMock.expectLastCall().once();
    
    EasyMock.replay(milterHandlerMock, milterContextMock);

    engine.submit(milterContextMock, packet);
    
    EasyMock.verify(milterHandlerMock, milterContextMock);
  }

  @Test public void testSubmitWithException() {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = MilterPacket.builder()
        .command((byte) 0x00)
        .build();

    milterContextMock.setSessionState(MilterState.UNKNOWN);
    EasyMock.expectLastCall().andThrow(new IllegalStateException());

    EasyMock.expect(milterContextMock.id()).andReturn(contextId).once();
    milterContextMock.setSessionState(MilterState.ABORT);
    EasyMock.expectLastCall().once();

    milterHandlerMock.abortSession(milterContextMock, packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }

  @Test public void testSubmitAbort() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_ABORT);

    milterHandlerMock.abort(EasyMock.eq(milterContextMock), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitBody() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    String bodyText = "test data\r\n";
    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_BODY
        , bodyText.getBytes(StandardCharsets.UTF_8));

    milterHandlerMock.body(EasyMock.eq(milterContextMock), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitConnect() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_CONNECT
        , HEX.toByteArray("5b3134342e3232392e3231302e39345d0034f3553134342e3232392e3231302e393400"));

    milterHandlerMock.connect(EasyMock.eq(milterContextMock), EasyMock.anyObject(), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitData() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    byte[] payload = HEX.toByteArray("54690031313331413641424542000000000154");
    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_DATA, payload);

    milterHandlerMock.data(EasyMock.eq(milterContextMock), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitEndOfBody() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_BODYEOB);

    milterHandlerMock.eom(EasyMock.eq(milterContextMock), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitEndOfHeader() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_EOH);

    milterHandlerMock.eoh(EasyMock.eq(milterContextMock));
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitEnvfrom() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_MAIL
        , HEX.toByteArray("3c737570706f7274406578616d706c652e6f72673e"
        + "0053495a453d3135353200424f44593d384249544d494d4500"));

    milterHandlerMock.envfrom(EasyMock.eq(milterContextMock), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitEnvrcpt() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_RCPT
        , HEX.toByteArray("3c636c69656e74406578616d706c652e6f72673e"
        + "004f524350543d7266633832323b636c69656e74406578616d706c652e6f726700"));

    milterHandlerMock.envrcpt(EasyMock.eq(milterContextMock), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitHeader() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_HEADER
        , HEX.toByteArray("46726f6d0020737570706f7274203c737570706f7274406578616d706c652e6f72673e00"));

    milterHandlerMock.header(EasyMock.eq(milterContextMock), EasyMock.anyObject(), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitHelo() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_HELO
        , HEX.toByteArray("6d61696c2e6578616d706c652e6f726700"));

    milterHandlerMock.helo(EasyMock.eq(milterContextMock), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitMacros() {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = MilterPacket.builder()
        .command((byte) CommandProcessor.SMFIC_MACRO)
        .payload(HEX.toByteArray("436a006d782e6578616d706c652e6f7267007b6461656d6f6e5f6e616d657d00"
            + "6d782e6578616d706c652e6f7267007600506f737466697820322e31302e3100"))
        .build();

    milterContextMock.setMacros(EasyMock.eq(CommandProcessor.SMFIC_CONNECT), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitOptneg() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_OPTNEG
        , HEX.toByteArray("00000006000001ff001fffff"));

    milterHandlerMock.negotiate(EasyMock.eq(milterContextMock)
        , EasyMock.eq(6), EasyMock.anyObject(), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitQuit() {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_QUIT);

    milterHandlerMock.close(EasyMock.eq(milterContextMock));
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }

  @Test public void testSubmitUnknown() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    CommandEngine engine = new CommandEngine(milterHandlerMock);

    MilterPacket packet = new MilterPacket((byte) CommandProcessor.SMFIC_UNKNOWN, HEX.toByteArray("c0febebe"));

    milterHandlerMock.unknown(EasyMock.eq(milterContextMock), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock);

    engine.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock);
  }
}
