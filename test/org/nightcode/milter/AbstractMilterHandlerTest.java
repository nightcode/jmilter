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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.net.MilterPacketSender;
import org.nightcode.milter.util.Hexs;

import org.junit.Assert;
import org.junit.Test;
import org.easymock.Capture;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_CONNECT;
import static org.nightcode.milter.CommandCode.SMFIC_EOB;
import static org.nightcode.milter.CommandCode.SMFIC_HEADER;
import static org.nightcode.milter.CommandCode.SMFIC_OPTNEG;

public class AbstractMilterHandlerTest {

  private static final Hexs HEX = Hexs.hex();

  @Test public void testNegotiate() throws MilterException, IOException {
    MilterPacketSender packetSenderMock = EasyMock.mock(MilterPacketSender.class);

    int           mtaProtocolVersion = 6;
    Actions       mtaActions         = new Actions(new byte[]{0x00, 0x00, 0x01, (byte) 0xFF}, 0);
    ProtocolSteps mtaProtocolSteps   = new ProtocolSteps(new byte[]{0x00, 0x1F, (byte) 0xFF, (byte) 0xFF}, 0);

    Actions milterActions = Actions.builder()
        .changeFrom()
        .addRecipients()
        .deleteRecipients()
        .addHeader()
        .changeDeleteHeaders()
        .build();
    ProtocolSteps milterProtocolSteps = ProtocolSteps.DEF_PROTOCOL_STEPS;

    MilterHandler handler = new AbstractMilterHandler(milterActions, milterProtocolSteps) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    MilterContext context = handler.createContext(packetSenderMock);

    Assert.assertNotNull(context.id());

    Capture<MilterPacket> packetCapture = EasyMock.newCapture();

    packetSenderMock.send(EasyMock.capture(packetCapture));
    EasyMock.expectLastCall().once();

    EasyMock.replay(packetSenderMock);

    context.setSessionStep(SMFIC_OPTNEG);
    handler.optneg(context, mtaProtocolVersion, mtaActions, mtaProtocolSteps);

    Assert.assertEquals(MilterContext.PROTOCOL_VERSION, context.milterProtocolVersion());
    Assert.assertEquals(milterActions, context.milterActions());
    Assert.assertEquals(milterProtocolSteps, context.milterProtocolSteps());

    Assert.assertEquals(mtaProtocolVersion, context.getMtaProtocolVersion());
    Assert.assertEquals(mtaActions, context.getMtaActions());
    Assert.assertEquals(mtaProtocolSteps, context.getMtaProtocolSteps());

    Assert.assertEquals(MilterContext.PROTOCOL_VERSION, context.getSessionProtocolVersion());
    Assert.assertEquals(milterProtocolSteps, context.getSessionProtocolSteps());

    MilterPacket target = packetCapture.getValue();
    Assert.assertEquals(SMFIC_OPTNEG.code(), target.command());
    Assert.assertEquals("00000006"
            + HEX.fromByteArray(context.milterActions().array())
            + HEX.fromByteArray(context.getSessionProtocolSteps().array())
        , HEX.fromByteArray(target.payload()));

    EasyMock.verify(packetSenderMock);
  }

  @Test public void testNegotiateAbortSession() throws MilterException {
    MilterPacketSender packetSenderMock = EasyMock.mock(MilterPacketSender.class);

    int           mtaProtocolVersion = 6;
    Actions       mtaActions         = Actions.DEF_ACTIONS;
    ProtocolSteps mtaProtocolSteps   = ProtocolSteps.DEF_PROTOCOL_STEPS;

    Actions milterActions = Actions.builder()
        .changeFrom()
        .addRecipients()
        .deleteRecipients()
        .addHeader()
        .changeDeleteHeaders()
        .build();
    ProtocolSteps milterProtocolSteps = ProtocolSteps.DEF_PROTOCOL_STEPS;

    MilterHandler handler = new AbstractMilterHandler(milterActions, milterProtocolSteps) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    MilterContext context = handler.createContext(packetSenderMock);

    Assert.assertNotNull(context.id());

    packetSenderMock.close();
    EasyMock.expectLastCall().once();

    EasyMock.replay(packetSenderMock);

    context.setSessionStep(SMFIC_OPTNEG);
    handler.optneg(context, mtaProtocolVersion, mtaActions, mtaProtocolSteps);

    Assert.assertEquals(MilterContext.PROTOCOL_VERSION, context.milterProtocolVersion());
    Assert.assertEquals(milterActions, context.milterActions());
    Assert.assertEquals(milterProtocolSteps, context.milterProtocolSteps());

    Assert.assertEquals(mtaProtocolVersion, context.getMtaProtocolVersion());
    Assert.assertEquals(mtaActions, context.getMtaActions());
    Assert.assertNull(context.getMtaProtocolSteps());

    Assert.assertEquals(MilterContext.PROTOCOL_VERSION, context.getSessionProtocolVersion());
    Assert.assertNull(context.getSessionProtocolSteps());

    EasyMock.verify(packetSenderMock);
  }

  @Test public void testAbortSession() {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    MilterPacket packet = MilterPacket.builder().command(SMFIC_CONNECT).build();

    EasyMock.expect(contextMock.getSessionStep()).andReturn(SMFIC_HEADER).once();
    EasyMock.expect(contextMock.getSessionStep()).andReturn(SMFIC_EOB).once();
    EasyMock.expect(contextMock.getSessionStep()).andReturn(SMFIC_HEADER).once();

    contextMock.destroy();
    EasyMock.expectLastCall().times(3);

    EasyMock.replay(contextMock);

    handler.abortSession(contextMock, packet);
    handler.abortSession(contextMock, packet);
    handler.abortSession(contextMock, packet);

    EasyMock.verify(contextMock);
  }

  @Test public void testConnect() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    contextMock.sendContinue();
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    handler.connect(contextMock, "localhost", ProtocolFamily.SMFIA_INET.code(), 23442, new InetSocketAddress("127.0.0.1", 23442));

    EasyMock.verify(contextMock);
  }

  @Test public void testHelo() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    contextMock.sendContinue();
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    handler.helo(contextMock, "bla-bla");

    EasyMock.verify(contextMock);
  }

  @Test public void testEnvfrom() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    contextMock.sendContinue();
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    handler.envfrom(contextMock, Collections.emptyList());

    EasyMock.verify(contextMock);
  }

  @Test public void testEnvrcpt() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    contextMock.sendContinue();
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    handler.envrcpt(contextMock, Collections.emptyList());

    EasyMock.verify(contextMock);
  }

  @Test public void testHeader() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    contextMock.sendContinue();
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    handler.header(contextMock, "headerName", "headerValue");

    EasyMock.verify(contextMock);
  }

  @Test public void testEoh() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    contextMock.sendContinue();
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    handler.eoh(contextMock);

    EasyMock.verify(contextMock);
  }

  @Test public void testBody() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    contextMock.sendContinue();
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    handler.body(contextMock, "bla-bla".getBytes(StandardCharsets.UTF_8));

    EasyMock.verify(contextMock);
  }

  @Test public void testEob() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    contextMock.sendContinue();
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    handler.eob(contextMock, null);

    EasyMock.verify(contextMock);
  }

  @Test public void testAbort() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    EasyMock.replay(contextMock);

    handler.abort(contextMock, null);

    EasyMock.verify(contextMock);
  }

  @Test public void testData() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };
    contextMock.sendContinue();
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    handler.data(contextMock, null);

    EasyMock.verify(contextMock);
  }

  @Test public void testUnknown() throws MilterException {
    MilterContext contextMock = EasyMock.mock(MilterContext.class);

    MilterHandler handler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };
    contextMock.sendContinue();
    EasyMock.expectLastCall().once();

    EasyMock.replay(contextMock);

    handler.unknown(contextMock, null);

    EasyMock.verify(contextMock);
  }
}
