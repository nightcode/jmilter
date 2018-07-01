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

package org.nightcode.milter.net;

import org.nightcode.common.service.Service;
import org.nightcode.common.service.ServiceManager;
import org.nightcode.milter.config.GatewayConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

public class MilterGatewayManagerTest {

  @Test public void testStartStop() throws IOException, InterruptedException, ExecutionException, TimeoutException {
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }
    GatewayConfig config = new GatewayConfig();
    config.setAddress("localhost");
    config.setPort(port);
    MilterGatewayManager manager = new MilterGatewayManager(config, null, ServiceManager.instance());

    Service.State state = manager.start().get(100, TimeUnit.MILLISECONDS);
    Assert.assertEquals(Service.State.RUNNING, state);
    
    state = manager.stop().get(100, TimeUnit.MILLISECONDS);
    Assert.assertEquals(Service.State.TERMINATED, state);
  }
}
