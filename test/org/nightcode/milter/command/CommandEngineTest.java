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

import org.nightcode.milter.Actions;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterContextImpl;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.ProtocolFamily;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.net.MilterPacketSender;
import org.nightcode.milter.util.Hexs;

import org.junit.Assert;
import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_ABORT;
import static org.nightcode.milter.CommandCode.SMFIC_BODY;
import static org.nightcode.milter.CommandCode.SMFIC_CONNECT;
import static org.nightcode.milter.CommandCode.SMFIC_DATA;
import static org.nightcode.milter.CommandCode.SMFIC_EOB;
import static org.nightcode.milter.CommandCode.SMFIC_EOH;
import static org.nightcode.milter.CommandCode.SMFIC_HEADER;
import static org.nightcode.milter.CommandCode.SMFIC_HELO;
import static org.nightcode.milter.CommandCode.SMFIC_MACRO;
import static org.nightcode.milter.CommandCode.SMFIC_MAIL;
import static org.nightcode.milter.CommandCode.SMFIC_OPTNEG;
import static org.nightcode.milter.CommandCode.SMFIC_QUIT;
import static org.nightcode.milter.CommandCode.SMFIC_QUIT_NC;
import static org.nightcode.milter.CommandCode.SMFIC_RCPT;
import static org.nightcode.milter.CommandCode.SMFIC_UNKNOWN;

public class CommandEngineTest {

  private static final Hexs HEX = Hexs.hex();

  private MilterContext context(MilterHandler handler) {
    return new MilterContextImpl(handler, Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS, null);
  }

  @Test public void testSubmit() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = MilterPacket.builder().command((byte) 0x00).build();

    handlerMock.unknown(context, packet.payload());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_UNKNOWN, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitWithException() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = MilterPacket.builder().command((byte) 0x00).build();

    handlerMock.unknown(context, packet.payload());
    EasyMock.expectLastCall().andThrow(new IllegalStateException());
    handlerMock.abortSession(context, packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_ABORT, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitAbort() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_ABORT);

    handlerMock.abort(EasyMock.eq(context), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitBody() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    String       bodyText = "test data\r\n";
    MilterPacket packet   = new MilterPacket(SMFIC_BODY, bodyText.getBytes(StandardCharsets.UTF_8));

    handlerMock.body(EasyMock.eq(context), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_BODY, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitConnect() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_CONNECT, HEX.toByteArray("5b3134342e3232392e3231302e39345d0034f3553134342e3232392e3231302e393400"));

    handlerMock.connect(EasyMock.eq(context), EasyMock.anyObject(), EasyMock.eq(ProtocolFamily.SMFIA_INET.code()), EasyMock.eq(62293), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_CONNECT, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitData() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    byte[]       payload = HEX.toByteArray("54690031313331413641424542000000000154");
    MilterPacket packet  = new MilterPacket(SMFIC_DATA, payload);

    handlerMock.data(EasyMock.eq(context), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_DATA, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitEob() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_EOB);

    handlerMock.eob(EasyMock.eq(context), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_EOB, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitEndOfHeader() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_EOH);

    handlerMock.eoh(context);
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_EOH, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitEnvfrom() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_MAIL, HEX.toByteArray("3c737570706f7274406578616d706c652e6f72673e" + "0053495a453d3135353200424f44593d384249544d494d4500"));

    handlerMock.envfrom(EasyMock.eq(context), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_MAIL, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitEnvrcpt() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_RCPT, HEX.toByteArray("3c636c69656e74406578616d706c652e6f72673e" + "004f524350543d7266633832323b636c69656e74406578616d706c652e6f726700"));

    handlerMock.envrcpt(EasyMock.eq(context), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_RCPT, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitHeader() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_HEADER, HEX.toByteArray("46726f6d0020737570706f7274203c737570706f7274406578616d706c652e6f72673e00"));

    handlerMock.header(EasyMock.eq(context), EasyMock.anyObject(), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_HEADER, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitHelo() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_HELO, HEX.toByteArray("6d61696c2e6578616d706c652e6f726700"));

    handlerMock.helo(EasyMock.eq(context), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_HELO, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitMacros() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = MilterPacket.builder().command(SMFIC_MACRO).payload(HEX.toByteArray("436a006d782e6578616d706c652e6f7267007b6461656d6f6e5f6e616d657d00" + "6d782e6578616d706c652e6f7267007600506f737466697820322e31302e3100")).build();

    handlerMock.macro(EasyMock.eq(context), EasyMock.eq(SMFIC_CONNECT.code()), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitOptneg() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_OPTNEG, HEX.toByteArray("00000006000001ff001fffff"));

    handlerMock.optneg(EasyMock.eq(context), EasyMock.eq(6), EasyMock.anyObject(), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_OPTNEG, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }

  @Test public void testSubmitQuit() {
    MilterHandler      handlerMock      = EasyMock.createMock(MilterHandler.class);
    MilterPacketSender packetSenderMock = EasyMock.mock(MilterPacketSender.class);
    MilterContext      context          = new MilterContextImpl(handlerMock, Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS, packetSenderMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_QUIT);

    handlerMock.quit(context);
    EasyMock.expectLastCall().once();
    packetSenderMock.close();
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock, packetSenderMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_QUIT, context.getSessionStep());

    EasyMock.verify(handlerMock, packetSenderMock);
  }

  @Test public void testSubmitQuitNc() {
    MilterHandler      handlerMock      = EasyMock.createMock(MilterHandler.class);
    MilterPacketSender packetSenderMock = EasyMock.mock(MilterPacketSender.class);
    MilterContext      context          = new MilterContextImpl(handlerMock, Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS, packetSenderMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_QUIT_NC);

    handlerMock.quitNc(EasyMock.eq(context));
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock, packetSenderMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_QUIT_NC, context.getSessionStep());

    EasyMock.verify(handlerMock, packetSenderMock);
  }

  @Test public void testSubmitUnknown() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    CommandEngine engine = CommandEngine.instance();

    MilterPacket packet = new MilterPacket(SMFIC_UNKNOWN, HEX.toByteArray("c0febebe"));

    handlerMock.unknown(EasyMock.eq(context), EasyMock.anyObject());
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    engine.submit(context, packet);
    Assert.assertEquals(SMFIC_UNKNOWN, context.getSessionStep());

    EasyMock.verify(handlerMock);
  }
}
