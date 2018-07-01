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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Test;

public class ConnectCommandProcessorTest {

  private static final Hexs HEX = Hexs.hex();

  private final UUID contextId = UUID.randomUUID();

  @Test public void testSubmit() throws UnknownHostException, MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    InetAddress actualAddress = InetAddress.getByName("144.229.210.94");
    MilterPacket packet = new MilterPacket((byte) 0x43
        , HEX.toByteArray("5b3134342e3232392e3231302e39345d0034f3553134342e3232392e3231302e393400"));

    ConnectCommandProcessor processor = new ConnectCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.CONNECT);
    EasyMock.expectLastCall().once();

    milterHandlerMock.connect(milterContextMock, "[144.229.210.94]", actualAddress);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }

  @Test public void testSubmitNullAddress() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet = new MilterPacket((byte) 0x43
        , HEX.toByteArray("5b3134342e3232392e3231302e39345d0036f3553134342e3232392e3231302e393400"));

    ConnectCommandProcessor processor = new ConnectCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.CONNECT);
    EasyMock.expectLastCall().once();

    milterHandlerMock.connect(milterContextMock, "[144.229.210.94]", null);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }

  @Test public void testCheckInvalidAddressValue() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet = new MilterPacket((byte) 0x43
        , HEX.toByteArray("5b3134342e3232392e3231302e39345d0034f3553134342e3239392e3231302e393400"));

    ConnectCommandProcessor processor = new ConnectCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.CONNECT);
    EasyMock.expectLastCall().once();
    EasyMock.expect(milterContextMock.id()).andReturn(contextId).once();

    milterHandlerMock.abortSession(milterContextMock, packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }

  @Test public void testCheckLastZeroTerm() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet = new MilterPacket((byte) 0x43
        , HEX.toByteArray("5b3134342e3232392e3231302e39345d0034f3553134342e3232392e3231302e3934"));

    ConnectCommandProcessor processor = new ConnectCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.CONNECT);
    EasyMock.expectLastCall().once();
    EasyMock.expect(milterContextMock.id()).andReturn(contextId).once();

    milterHandlerMock.abortSession(milterContextMock, packet);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }

  @Test public void testCheckWrongPacketLength() throws MilterException {
    MilterHandler milterHandlerMock = EasyMock.createMock(MilterHandler.class);
    MilterContext milterContextMock = EasyMock.createMock(MilterContext.class);

    MilterPacket packet1 = new MilterPacket((byte) 0x43, HEX.toByteArray("5b3134342e3232392e3231302e39345d00"));
    MilterPacket packet2 = new MilterPacket((byte) 0x43, HEX.toByteArray("5b3134342e3232392e3231302e39345d003400"));

    ConnectCommandProcessor processor = new ConnectCommandProcessor(milterHandlerMock);

    milterContextMock.setSessionState(MilterState.CONNECT);
    EasyMock.expectLastCall().times(2);
    EasyMock.expect(milterContextMock.id()).andReturn(contextId).times(2);

    milterHandlerMock.abortSession(milterContextMock, packet1);
    EasyMock.expectLastCall().once();
    milterHandlerMock.abortSession(milterContextMock, packet2);
    EasyMock.expectLastCall().once();

    EasyMock.replay(milterHandlerMock, milterContextMock);

    processor.submit(milterContextMock, packet1);
    processor.submit(milterContextMock, packet2);

    EasyMock.verify(milterHandlerMock, milterContextMock);
  }
}
