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

package org.nightcode.milter.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.nightcode.milter.AbstractMilterHandler;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.Actions;
import org.nightcode.milter.ProtocolSteps;

import org.junit.Assert;
import org.junit.Test;

public class MilterGatewayManagerTest {

  @Test public void testStartStop() throws IOException, InterruptedException, ExecutionException, TimeoutException {
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }
    InetSocketAddress address = new InetSocketAddress("localhost", port);

    MilterHandler milterHandler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    MilterGatewayManager gatewayManager;
    try (MilterGatewayManager manager = new MilterGatewayManager(address, milterHandler)) {
      gatewayManager = manager;
      gatewayManager.bind().get(500, TimeUnit.MILLISECONDS);
      Assert.assertEquals(MilterGatewayManager.RUNNING, gatewayManager.getState());
    }
    Assert.assertNotNull(gatewayManager);
    Assert.assertEquals(MilterGatewayManager.CLOSED, gatewayManager.getState());
  }
}
