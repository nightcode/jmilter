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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.netty.channel.local.LocalAddress;
import org.nightcode.milter.AbstractMilterHandler;
import org.nightcode.milter.Actions;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.client.ConnectionFactory;
import org.nightcode.milter.client.LocalConnectionFactory;
import org.nightcode.milter.client.MilterSessionFactory;
import org.nightcode.milter.client.MilterSessionFactoryBuilder;

import org.junit.Assert;
import org.junit.Test;

import static org.nightcode.milter.ProtocolFamily.SMFIA_INET;

public class MilterGatewayManagerTest {

  @Test public void testStartStop() throws Exception {
    MilterHandler milterHandler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    MilterGatewayManager<LocalAddress> gatewayManager;
    try (MilterGatewayManager<LocalAddress> manager = new MilterGatewayManager<>(new LocalServerFactory(), milterHandler)) {
      gatewayManager = manager;
      gatewayManager.bind().get(500, TimeUnit.MILLISECONDS);
      Assert.assertEquals(MilterGatewayManager.RUNNING, gatewayManager.getState());
    }
    Assert.assertNotNull(gatewayManager);
    Assert.assertEquals(MilterGatewayManager.CLOSED, gatewayManager.getState());
  }

  @Test public void testSessionInitializer() throws Exception {
    MilterHandler milterHandler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    MilterGatewayManager<LocalAddress> gatewayManager;
    try (MilterGatewayManager<LocalAddress> manager = new MilterGatewayManager<>(new LocalServerFactory(), milterHandler)) {
      gatewayManager = manager;
      gatewayManager.bind().get(500, TimeUnit.MILLISECONDS);
      Assert.assertEquals(MilterGatewayManager.RUNNING, gatewayManager.getState());

      ConnectionFactory<LocalAddress> connectionFactory = new LocalConnectionFactory();

      MilterSessionFactoryBuilder<LocalAddress> builder = MilterSessionFactoryBuilder.<LocalAddress>builder()
          .factory(connectionFactory)
          .protocolVersion(6)
          .actions(Actions.DEF_ACTIONS)
          .protocolSteps(ProtocolSteps.DEF_PROTOCOL_STEPS);

      try (MilterSessionFactory factory = builder.create()) {
        List<String> envfrom = new ArrayList<>();
        envfrom.add("<support@example.org>");

        List<String> envrcpt = new ArrayList<>();
        envrcpt.add("<client@example.org>");

        factory.createSession()
            .thenCompose(s -> s.connect("[144.229.210.94]", SMFIA_INET, 62293, "144.229.210.94"))
            .thenCompose(r -> r.session().helo("mail.example.org"))
            .thenCompose(r -> r.session().envfrom(envfrom))
            .thenCompose(r -> r.session().envrcpt(envrcpt))
            .thenCompose(r -> r.session().header("Subject", "Test"))
            .thenCompose(r -> r.session().header("Message-Id", UUID.randomUUID() + "@example.org"))
            .thenCompose(r -> r.session().eoh())
            .thenCompose(r -> r.session().body("some text".getBytes(StandardCharsets.UTF_8)))
            .thenCompose(r -> r.session().eob())
            .thenCompose(r -> r.session().abort())
            .thenCompose(r -> r.session().abort())
            .thenCompose(r -> r.session().quit())
            .get(500, TimeUnit.MILLISECONDS);

        factory.createSession()
            .thenCompose(s -> s.connect("[144.229.210.94]", SMFIA_INET, 62293, "144.229.210.94"))
            .thenCompose(r -> r.session().helo("mail.example.org"))
            .thenCompose(r -> r.session().envfrom(envfrom))
            .thenCompose(r -> r.session().envrcpt(envrcpt))
            .thenCompose(r -> r.session().header("Subject", "Test"))
            .thenCompose(r -> r.session().header("Message-Id", UUID.randomUUID() + "@example.org"))
            .thenCompose(r -> r.session().eoh())
            .thenCompose(r -> r.session().body("some text".getBytes(StandardCharsets.UTF_8)))
            .thenCompose(r -> r.session().eob())
            .thenCompose(r -> r.session().abort())
            .thenCompose(r -> r.session().abort())
            .thenCompose(r -> r.session().quit())
            .get(500, TimeUnit.MILLISECONDS);
      }
    }
  }
}
