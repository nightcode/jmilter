# JMilter

[![Build Status](https://travis-ci.org/nightcode/jmilter.svg?branch=master)](https://travis-ci.org/nightcode/jmilter)
[![GitHub license](https://img.shields.io/github/license/nightcode/jmilter.svg)](https://github.com/nightcode/jmilter/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.nightcode/jmilter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.nightcode%20AND%20a%3Ajmilter)

Java implementation of the Sendmail Milter protocol. The details of the Milter protocol can be found [here][1].
An introduction of using milter functionality with Postfix is available in the [Postfix before-queue Milter support][2] article.

How to use
----------

Milter side

```java
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

  // gateway address
  String address = System.getProperty("jmilter.address", "0.0.0.0:4545");

  MilterGatewayManager gatewayManager = new MilterGatewayManager(address, milterHandler, ServiceManager.instance());

  gatewayManager.start();
```

MTA side

```java
CompletableFuture<Void> call(MilterSessionFactory factory) {
  Macros connectionMacros = Macros.builder()
      .add("j", "mx.example.org")
      .add("{daemon_name}", "mx.example.org")
      .add("v", "Postfix 2.10.1")
      .build();

  Macros fromMacros = Macros.builder()
      .add("{mail_mailer}", "smtp")
      .add("{mail_host}", "mx1.example.org")
      .add("{mail_addr}", "sender@example.org")
      .build();

  Macros rcptMacros = Macros.builder()
      .add("{rcpt_mailer}", "smtp")
      .add("{rcpt_host}", "mx1.example.com")
      .add("{rcpt_addr}", "sender@example.com")
      .build()

  Macros headerMacros = Macros.builder().add("i", "C1A7C20BAF").build();

  List<String> envfrom = new ArrayList<>();
  envfrom.add("<sender@example.org>");
  envfrom.add("SIZE=1552");
  envfrom.add("BODY=8BITMIME");

  List<String> envrcpt = new ArrayList<>();
  envrcpt.add("<recipient@example.com>");
  envrcpt.add("ORCPT=rfc822;recipient@example.com");

  return factory.createSession()
      .thenCompose(s -> s.connect("[88.88.88.88]", SMFIA_INET, 4567, "88.88.88.88", connectionMacros))
      .thenCompose(r -> r.session().helo("mail.example.org"))
      .thenCompose(r -> r.session().envfrom(envfrom, fromMacros))
      .thenCompose(r -> r.session().envrcpt(envrcpt, rcptMacros))
      .thenCompose(r -> r.session().header("Subject", "Some subject", headerMacros))
      .thenCompose(r -> r.session().header("From", "sender@example.org", headerMacros))
      .thenCompose(r -> r.session().header("To", "recipient@example.com", headerMacros))
      .thenCompose(r -> r.session().header("Message-Id", UUID.randomUUID() + "@example.org", headerMacros))
      .thenCompose(r -> r.session().eoh())
      .thenCompose(r -> r.session().body("Some text".getBytes(StandardCharsets.UTF_8)))
      .thenCompose(r -> r.session().eob())
      .thenCompose(r -> r.session().abort())
      .thenCompose(r -> r.session().quit());
}

```

The test folder contains the complete example code.

#### Available options

| Name                             | Possible values                 |
|----------------------------------|---------------------------------|
| jmilter.netty.loggingEnabled     | true, false                     |
| jmilter.netty.logLevel           | TRACE, DEBUG, INFO, WARN, ERROR |
| jmilter.netty.nThreads           | [0, 65535]                      |
| jmilter.netty.connectTimeoutMs   | [0, Long.MAX_VALUE]             |
| jmilter.netty.reconnectTimeoutMs | [0, Long.MAX_VALUE]             |
| jmilter.netty.autoRead           | true, false                     |
| jmilter.netty.keepAlive          | true, false                     |
| jmilter.netty.tcpNoDelay         | true, false                     |
| jmilter.netty.reuseAddress       | true, false                     |
| jmilter.netty.soBacklog          | [0, 65535]                      |

Download
--------

Download [the latest release][3] via Maven:
```xml
<dependency>
  <groupId>org.nightcode</groupId>
  <artifactId>jmilter</artifactId>
  <version>0.3.7</version>
</dependency>
```

Feedback is welcome. Please don't hesitate to open up a new [github issue](https://github.com/nightcode/jmilter/issues) or simply drop me a line at <dmitry@nightcode.org>.


 [1]: https://raw.githubusercontent.com/avar/sendmail-pmilter/master/doc/milter-protocol.txt
 [2]: http://www.postfix.org/MILTER_README.html
 [3]: https://oss.sonatype.org/service/local/artifact/maven/redirect?r=releases&g=org.nightcode&a=jmilter&v=LATEST
