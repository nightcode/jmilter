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

package org.nightcode.milter.client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.nightcode.milter.Actions;
import org.nightcode.milter.ProtocolFamily;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.util.Hexs;

import org.junit.Assert;
import org.junit.Test;

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

public class MilterPacketFactoryTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testCreateAbort() {
    MilterPacket packet = MilterPacketFactory.createAbort();

    Assert.assertEquals(SMFIC_ABORT.code(), packet.command());
    Assert.assertEquals(0, packet.payload().length);
  }

  @Test public void testCreateBodyChunk() {
    String       bodyText = "test data\r\n";
    MilterPacket packet   = MilterPacketFactory.createBodyChunk(bodyText.getBytes(StandardCharsets.UTF_8));

    Assert.assertEquals(SMFIC_BODY.code(), packet.command());
    Assert.assertEquals(bodyText, new String(packet.payload(), StandardCharsets.UTF_8));
  }

  @Test public void testCreateConnect() {
    MilterPacket packet = MilterPacketFactory.createConnect("[144.229.210.94]", ProtocolFamily.SMFIA_INET, 62293, "144.229.210.94");

    Assert.assertEquals(SMFIC_CONNECT.code(), packet.command());
    Assert.assertArrayEquals(HEX.toByteArray("5b3134342e3232392e3231302e39345d0034f3553134342e3232392e3231302e393400"), packet.payload());
  }

  @Test public void testCreateData() {
    byte[]       payload = HEX.toByteArray("54690031313331413641424542000000000154");
    MilterPacket packet  = MilterPacketFactory.createData(payload);

    Assert.assertEquals(SMFIC_DATA.code(), packet.command());
    Assert.assertArrayEquals(payload, packet.payload());
  }

  @Test public  void testCreateEnvfrom() {
    List<String> list = new ArrayList<>();
    list.add("<support@example.org>");
    list.add("SIZE=1552");
    list.add("BODY=8BITMIME");

    MilterPacket packet = MilterPacketFactory.createEnvfrom(list);

    Assert.assertEquals(SMFIC_MAIL.code(), packet.command());
    Assert.assertArrayEquals(HEX.toByteArray("3c737570706f7274406578616d706c652e6f72673e0053495a453d3135353200424f44593d384249544d494d4500")
        , packet.payload());
  }

  @Test public void testCreateEnvrcpt() {
    List<String> list = new ArrayList<>();
    list.add("<client@example.org>");
    list.add("ORCPT=rfc822;client@example.org");

    MilterPacket packet = MilterPacketFactory.createEnvrcpt(list);

    Assert.assertEquals(SMFIC_RCPT.code(), packet.command());
    Assert.assertArrayEquals(HEX.toByteArray("3c636c69656e74406578616d706c652e6f72673e004f524350543d7266633832323b636c69656e74406578616d706c652e6f726700")
        , packet.payload());
  }

  @Test public void testCreateEob() {
    MilterPacket packet = MilterPacketFactory.createEob();

    Assert.assertEquals(SMFIC_EOB.code(), packet.command());
    Assert.assertEquals(0, packet.payload().length);
  }

  @Test public void testCreateEoh() {
    MilterPacket packet = MilterPacketFactory.createEoh();

    Assert.assertEquals(SMFIC_EOH.code(), packet.command());
    Assert.assertEquals(0, packet.payload().length);
  }

  @Test public void testCreateHeader() {
    MilterPacket packet = MilterPacketFactory.createHeader("From", " support <support@example.org>");

    Assert.assertEquals(SMFIC_HEADER.code(), packet.command());
    Assert.assertArrayEquals(HEX.toByteArray("46726f6d0020737570706f7274203c737570706f7274406578616d706c652e6f72673e00")
        , packet.payload());
  }

  @Test public void testCreateHelo() {
    MilterPacket packet = MilterPacketFactory.createHelo("mail.example.org");

    Assert.assertEquals(SMFIC_HELO.code(), packet.command());
    Assert.assertArrayEquals(HEX.toByteArray("6d61696c2e6578616d706c652e6f726700"), packet.payload());
  }

  @Test public void testCreateMacro() {
    Macros macros = Macros.builder()
        .add("j", "mx.example.org")
        .add("{daemon_name}", "mx.example.org")
        .add("v", "Postfix 2.10.1")
        .build();

    MilterPacket packet = MilterPacketFactory.createMacro(SMFIC_CONNECT, macros);

    Assert.assertEquals(SMFIC_MACRO.code(), packet.command());
    Assert.assertArrayEquals(HEX.toByteArray("436a006d782e6578616d706c652e6f7267007b6461656d6f6e5f6e616d657d006d782e6578616d706c652e6f7267007600506f737466697820322e31302e3100")
        , packet.payload());
  }

  @Test public void testCreateOptneg() {
    byte[] payload = HEX.toByteArray("00000006000001ff001fffff");

    int           version       = 6;
    Actions       actions       = new Actions(payload, 4);
    ProtocolSteps protocolSteps = new ProtocolSteps(payload, 8);
    
    MilterPacket packet = MilterPacketFactory.createOptneg(version, actions, protocolSteps);

    Assert.assertEquals(SMFIC_OPTNEG.code(), packet.command());
    Assert.assertArrayEquals(payload, packet.payload());
  }

  @Test public void testCreateQuit() {
    MilterPacket packet = MilterPacketFactory.createQuit();

    Assert.assertEquals(SMFIC_QUIT.code(), packet.command());
    Assert.assertEquals(0, packet.payload().length); 
  }

  @Test public void testCreateQuitNc() {
    MilterPacket packet = MilterPacketFactory.createQuitNc();

    Assert.assertEquals(SMFIC_QUIT_NC.code(), packet.command());
    Assert.assertEquals(0, packet.payload().length);
  }
}
