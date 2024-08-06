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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
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
import org.nightcode.milter.MilterOptions;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.client.ConnectionFactory;
import org.nightcode.milter.client.LocalConnectionFactory;
import org.nightcode.milter.client.MilterSessionFactory;
import org.nightcode.milter.client.MilterSessionFactoryBuilder;
import org.nightcode.milter.util.NetUtils;
import org.nightcode.milter.util.Properties;

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

  @Test public void testFailStopMode() throws Exception {
    Field field = Properties.class.getDeclaredField("OPTIONS");
    field.setAccessible(true);
    Method method = field.getType().getMethod("clear");
    method.invoke(field.get(Properties.class));

    System.setProperty(MilterOptions.NETTY_FAIL_STOP_MODE.key(), "true");

    MilterHandler milterHandler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
      @Override public void quit(MilterContext context) {
        // do nothing
      }
    };

    try (ServerSocket socket = new ServerSocket(0)) {
      int port = socket.getLocalPort();

      InetSocketAddress address = NetUtils.parseAddress(System.getProperty("jmilter.address", "0.0.0.0:" + port));

      ServerFactory<InetSocketAddress> serverFactory = ServerFactory.tcpIpFactory(address);

      try (MilterGatewayManager<InetSocketAddress> gatewayManager = new MilterGatewayManager<>(serverFactory, milterHandler)) {
        gatewayManager.bind().get(500, TimeUnit.MILLISECONDS);
        Assert.fail("should throw Exception");
      } catch (Exception ex) {
        Assert.assertEquals("java.net.BindException: Address already in use", ex.getMessage());
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
