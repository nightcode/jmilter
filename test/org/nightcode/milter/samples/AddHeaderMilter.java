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

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.net.MilterGatewayManager;
import org.nightcode.milter.Actions;
import org.nightcode.milter.util.NetUtils;
import org.nightcode.milter.ProtocolSteps;

public final class AddHeaderMilter {

  public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
    InetSocketAddress address = NetUtils.parseAddress(System.getProperty("jmilter.address", "0.0.0.0:4545"));

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
    MilterHandler milterHandler = new AddHeaderMilterHandler(milterActions, milterProtocolSteps);

    try (MilterGatewayManager gatewayManager = new MilterGatewayManager(address, milterHandler)) {
      gatewayManager.bind().get(500, TimeUnit.MILLISECONDS);
    }
  }
}
