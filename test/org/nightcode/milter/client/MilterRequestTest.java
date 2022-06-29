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

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.timeout.ReadTimeoutException;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.codec.MilterPacket;

import org.junit.Assert;
import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_EOH;
import static org.nightcode.milter.CommandCode.SMFIC_HELO;
import static org.nightcode.milter.util.MilterPackets.SMFIS_CONTINUE;

public class MilterRequestTest {

  @Test public void testResponse() throws Exception {
    MilterSession   sessionMock = EasyMock.mock(MilterSession.class);
    EmbeddedChannel channel     = new EmbeddedChannel();

    CompletableFuture<MilterResponse> responseFuture = new CompletableFuture<>();

    EasyMock.expect(sessionMock.milterProtocolSteps()).andReturn(ProtocolSteps.DEF_PROTOCOL_STEPS).once();
    EasyMock.replay(sessionMock);

    MilterRequest request = new MilterRequest(
        SMFIC_HELO
        , new MilterPacket[] {MilterPacketFactory.createHelo("mail.example.org")}
        , sessionMock
        , channel
        , responseFuture
    );

    request.execute();
    request.onAction(SMFIS_CONTINUE);

    MilterResponse response = responseFuture.get(500, TimeUnit.MILLISECONDS);

    channel.advanceTimeBy(SMFIC_HELO.responseTimeoutMs(), TimeUnit.MILLISECONDS);
    channel.runScheduledPendingTasks();

    Assert.assertEquals(SMFIS_CONTINUE, response.lastPacket());
    Assert.assertTrue(request.timeoutFuture().isCancelled());

    EasyMock.verify(sessionMock);
  }

  @Test public void testReadTimeout() {
    System.setProperty("jmilter.SMFIC_HELO.responseTimeoutMs", "500");

    MilterSession   sessionMock = EasyMock.mock(MilterSession.class);
    EmbeddedChannel channel     = new EmbeddedChannel();

    CompletableFuture<MilterResponse> responseFuture = new CompletableFuture<>();

    EasyMock.expect(sessionMock.milterProtocolSteps()).andReturn(ProtocolSteps.DEF_PROTOCOL_STEPS).once();
    EasyMock.expect(sessionMock.id()).andReturn(UUID.randomUUID().toString()).once();
    EasyMock.replay(sessionMock);

    MilterRequest request = new MilterRequest(
        SMFIC_HELO
        , new MilterPacket[] {MilterPacketFactory.createHelo("mail.example.org")}
        , sessionMock
        , channel
        , responseFuture
    );

    request.execute();
    channel.advanceTimeBy(SMFIC_HELO.responseTimeoutMs(), TimeUnit.MILLISECONDS);
    channel.runScheduledPendingTasks();

    try {
      responseFuture.get(500, TimeUnit.MILLISECONDS);
      Assert.fail("should throw ReadTimeoutException");
    } catch (Exception ex) {
      Assert.assertTrue(ex.getCause() instanceof ReadTimeoutException);
    }

    EasyMock.verify(sessionMock);
  }

  @Test public void testToString() {
    MilterSession   sessionMock = EasyMock.mock(MilterSession.class);
    EmbeddedChannel channel     = new EmbeddedChannel();

    CompletableFuture<MilterResponse> responseFuture = new CompletableFuture<>();

    EasyMock.expect(sessionMock.milterProtocolSteps()).andReturn(ProtocolSteps.DEF_PROTOCOL_STEPS).once();
    EasyMock.replay(sessionMock);

    MilterRequest request = new MilterRequest(
        SMFIC_EOH
        , new MilterPacket[] {MilterPacketFactory.createMacro(SMFIC_EOH, Macros.builder().build()), MilterPacketFactory.createEoh()}
        , sessionMock
        , channel
        , responseFuture
    );

    Assert.assertEquals("MilterRequest{command=SMFIC_EOH" +
            ", packets=[MilterPacket{command=0x44 'D', payload=4E}, MilterPacket{command=0x4e 'N', payload=EMPTY}]}"
        , request.toString());

    EasyMock.verify(sessionMock);
  }
  
  @Test public void testOnFailure() throws Exception {
    MilterSession   sessionMock = EasyMock.mock(MilterSession.class);
    EmbeddedChannel channel     = new EmbeddedChannel();

    CompletableFuture<MilterResponse> responseFuture = new CompletableFuture<>();

    EasyMock.expect(sessionMock.milterProtocolSteps()).andReturn(ProtocolSteps.DEF_PROTOCOL_STEPS).once();
    EasyMock.replay(sessionMock);

    MilterRequest request = new MilterRequest(
        SMFIC_HELO
        , new MilterPacket[] {MilterPacketFactory.createHelo("mail.example.org")}
        , sessionMock
        , channel
        , responseFuture
    );

    IOException ioEx = new IOException("IO exception");
    request.onFailure(ioEx);

    try {
      responseFuture.get(100, TimeUnit.MILLISECONDS);
      Assert.fail("should throw Exception");
    } catch (Exception ex) {
      Assert.assertSame(ioEx, ex.getCause());
    }

    EasyMock.verify(sessionMock);
  }
}
