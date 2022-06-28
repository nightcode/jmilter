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

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.ProtocolFamily;
import org.nightcode.milter.codec.MilterPacket;

import org.junit.Test;
import org.easymock.EasyMock;

import static org.nightcode.milter.CommandCode.SMFIC_CONNECT;

public class ConnectCommandProcessorTest extends AbstractCommandProcessorTest {

  @Test public void testSubmit() throws UnknownHostException, MilterException {
    InetSocketAddress actualAddress = new InetSocketAddress("144.229.210.94", 62293);
    MilterPacket packet = new MilterPacket(SMFIC_CONNECT
        , HEX.toByteArray("5b3134342e3232392e3231302e39345d0034f3553134342e3232392e3231302e393400"));

    execute(packet, new ConnectCommandProcessor(), ctx -> {
      try {
        ctx.handler().connect(ctx, "[144.229.210.94]", ProtocolFamily.SMFIA_INET.code(), 62293, actualAddress);
        EasyMock.expectLastCall().once();
      } catch (MilterException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Test public void testSubmitNullAddress() throws MilterException {
    MilterPacket packet = new MilterPacket(SMFIC_CONNECT
        , HEX.toByteArray("5b3134342e3232392e3231302e39345d0036f3553134342e3232392e3231302e393400"));

    execute(packet, new ConnectCommandProcessor(), ctx -> {
      try {
        ctx.handler().connect(ctx, "[144.229.210.94]", ProtocolFamily.SMFIA_INET6.code(), 0, null);
        EasyMock.expectLastCall().once();
      } catch (MilterException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Test public void testCheckInvalidAddressValue() throws MilterException {
    MilterPacket packet = new MilterPacket(SMFIC_CONNECT
        , HEX.toByteArray("5b3134342e3232392e3231302e39345d0034f3553134342e3239392e3231302e393400"));

    execute(packet, new ConnectCommandProcessor(), ctx -> {
      ctx.handler().abortSession(ctx, packet);
      EasyMock.expectLastCall().once();
    });
  }

  @Test public void testCheckLastZeroTerm() throws MilterException {
    MilterPacket packet = new MilterPacket(SMFIC_CONNECT
        , HEX.toByteArray("5b3134342e3232392e3231302e39345d0034f3553134342e3232392e3231302e3934"));

    execute(packet, new ConnectCommandProcessor(), ctx -> {
      ctx.handler().abortSession(ctx, packet);
      EasyMock.expectLastCall().once();
    });
  }

  @Test public void testCheckWrongPacketLength() throws MilterException {
    MilterHandler handlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext context     = context(handlerMock);

    MilterPacket packet1 = new MilterPacket(SMFIC_CONNECT, HEX.toByteArray("5b3134342e3232392e3231302e39345d00"));
    MilterPacket packet2 = new MilterPacket(SMFIC_CONNECT, HEX.toByteArray("5b3134342e3232392e3231302e39345d003400"));

    CommandProcessor processor = new ConnectCommandProcessor();

    handlerMock.abortSession(context, packet1);
    EasyMock.expectLastCall().once();

    handlerMock.abortSession(context, packet2);
    EasyMock.expectLastCall().once();

    EasyMock.replay(handlerMock);

    processor.submit(context, packet1);
    processor.submit(context, packet2);

    EasyMock.verify(handlerMock);
  }
}
