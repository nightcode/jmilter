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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import org.nightcode.milter.AbstractMilterHandler;
import org.nightcode.milter.Actions;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.MilterOptions;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.net.MilterChannelHandler;
import org.nightcode.milter.net.SessionInitializer;

import org.junit.Test;

import static org.nightcode.milter.CommandCode.SMFIC_DATA;
import static org.nightcode.milter.ProtocolFamily.SMFIA_INET;

public class ClientTest {

  @Test public void test() throws Exception {
    System.setProperty(MilterOptions.NETTY_LOGGING_ENABLED.key(), "true");

    ConnectionFactory<LocalAddress> connectionFactory = new LocalConnectionFactory();
    ServerBootstrap                 serverBootstrap   = new ServerBootstrap();

    try {
      ProtocolSteps protocolSteps = ProtocolSteps.builder()
          .noConnect()
          .noHeaders()
          .noReplyForEoh()
          .build();

      MilterHandler milterHandler = new AbstractMilterHandler(Actions.DEF_ACTIONS, protocolSteps) {
        @Override public void quit(MilterContext context) {
          // do nothing
        }
      };
      
      serverBootstrap.group(new NioEventLoopGroup(2))
          .channel(LocalServerChannel.class)
          .childHandler(new SessionInitializer(new MilterChannelHandler(milterHandler)));

      serverBootstrap.bind(connectionFactory.remoteAddress()).sync();

      try (MilterSessionFactory factory = MilterSessionFactoryBuilder.<LocalAddress>builder()
          .factory(connectionFactory)
          .create()) {

        Macros connectionMacros = Macros.builder()
            .add("j", "mx.example.org")
            .add("{daemon_name}", "mx.example.org")
            .add("v", "Postfix 2.10.1")
            .build();

        Macros heloMacros = Macros.builder()
            .add("{tls_version}", "mx.example.org")
            .add("{cipher}", "mx.example.org")
            .add("{cipher_bits}", "Postfix 2.10.1")
            .add("{cert_subject}", "Postfix 2.10.1")
            .add("{cert_issuer}", "Postfix 2.10.1")
            .build();

        Macros mailMacros = Macros.builder()
            .add("{mail_mailer}", "smtp")
            .add("{mail_host}", "mx1.example.com")
            .add("{mail_addr}", "root@example.com")
            .build();

        Macros rcptMacros = Macros.builder()
            .add("{rcpt_mailer}", "smtp")
            .add("{rcpt_host}", "mx1.example.com")
            .add("{rcpt_addr}", "sender@example.com")
            .build();

        Macros headerMacros = Macros.builder().add("i", "C1A7C20BAF").build();

        List<String> envfrom = new ArrayList<>();
        envfrom.add("<support@example.org>");
        envfrom.add("SIZE=1552");
        envfrom.add("BODY=8BITMIME");

        List<String> envrcpt = new ArrayList<>();
        envrcpt.add("<client@example.org>");
        envrcpt.add("ORCPT=rfc822;client@example.org");

        factory.createSession(6, Actions.DEF_ACTIONS, protocolSteps)
            .thenCompose(s -> s.connect("[144.229.210.94]", SMFIA_INET, 62293, "144.229.210.94", connectionMacros))
            .thenCompose(r -> r.session().helo("mail.example.org", heloMacros))
            .thenCompose(r -> r.session().envfrom(envfrom, mailMacros))
            .thenCompose(r -> r.session().envrcpt(envrcpt, rcptMacros))
            .thenCompose(r -> r.session().macro(SMFIC_DATA, headerMacros))
            .thenCompose(r -> r.session().header("Subject", "Test", headerMacros))
            .thenCompose(r -> r.session().header("From", "sender@example.com", headerMacros))
            .thenCompose(r -> r.session().header("To", "client@example.org", headerMacros))
            .thenCompose(r -> r.session().header("Message-Id", UUID.randomUUID() + "@example.org", headerMacros))
            .thenCompose(r -> r.session().eoh())
            .thenCompose(r -> r.session().body("some text".getBytes(StandardCharsets.UTF_8)))
            .thenCompose(r -> r.session().body("some text 2".getBytes(StandardCharsets.UTF_8)))
            .thenCompose(r -> r.session().eob())
            .thenCompose(r -> r.session().abort())
            .thenCompose(r -> r.session().abort())
            .thenCompose(r -> r.session().quit())
            .get(500, TimeUnit.MILLISECONDS);
      }
    } finally {
      serverBootstrap.config().group().shutdownGracefully();
      serverBootstrap.config().childGroup().shutdownGracefully();
    }
  }
}
