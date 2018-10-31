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

package org.nightcode.milter;

import org.nightcode.common.service.ServiceManager;
import org.nightcode.milter.config.GatewayConfig;
import org.nightcode.milter.net.MilterChannelHandler;
import org.nightcode.milter.net.MilterGatewayManager;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.ProtocolSteps;

public final class ExampleMilter {

  public static void main(String[] args) {
    GatewayConfig gatewayConfig = new GatewayConfig();
    gatewayConfig.setAddress("0.0.0.0");
    gatewayConfig.setPort(4545);
    gatewayConfig.setTcpLoggingEnabled(true);
    gatewayConfig.setTcpLogLevel("DEBUG");

    // indicates what changes you intend to do with messages
    Actions milterActions = Actions.builder()
        .addHeader()
        .build();

    // indicates which steps you want to skip
    ProtocolSteps milterProtocolSteps = ProtocolSteps.builder()
        .noHelo()
        .noData()
        .noBody()
        .build();

    // a simple milter handler that only adds header "X-Received"
    MilterHandler milterHandler = new ExampleMilterHandler(milterActions, milterProtocolSteps);

    MilterGatewayManager gatewayManager = new MilterGatewayManager(gatewayConfig
        , () -> new MilterChannelHandler(milterHandler), ServiceManager.instance());

    gatewayManager.start();
  }
}
