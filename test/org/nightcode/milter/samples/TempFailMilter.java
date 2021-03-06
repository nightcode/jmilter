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

package org.nightcode.milter.samples;

import org.nightcode.common.service.ServiceManager;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.net.MilterGatewayManager;
import org.nightcode.milter.util.Actions;
import org.nightcode.milter.util.ProtocolSteps;

import java.net.UnknownHostException;

public final class TempFailMilter {

  public static void main(String[] args) throws UnknownHostException {
    String address = System.getProperty("jmilter.address", "0.0.0.0:4545");

    // indicates what changes you intend to do with messages
    Actions milterActions = Actions.builder()
        .build();

    // indicates which steps you want to skip
    ProtocolSteps milterProtocolSteps = ProtocolSteps.builder()
        .noHelo()
        .noData()
        .noBody()
        .build();

    // a simple milter handler that constantly returns a temporary failure status
    MilterHandler milterHandler = new TempFailMilterHandler(milterActions, milterProtocolSteps);

    MilterGatewayManager gatewayManager = new MilterGatewayManager(address, milterHandler, ServiceManager.instance());
    gatewayManager.start();
  }
}
