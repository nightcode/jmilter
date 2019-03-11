# JMilter

[![Build Status](https://travis-ci.org/nightcode/jmilter.svg?branch=master)](https://travis-ci.org/nightcode/jmilter)
[![Maven Central](https://img.shields.io/maven-central/v/org.nightcode/jmilter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.nightcode%20AND%20a%3Ajmilter)

Java implementation of the Sendmail Milter protocol.

How to use
----------

code

```java
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
```

The test folder contains the complete example code.

Download
--------

Download [the latest jar][1] via Maven:
```xml
<dependency>
  <groupId>org.nightcode</groupId>
  <artifactId>jmilter</artifactId>
  <version>0.2.1</version>
</dependency>
```

Feedback is welcome. Please don't hesitate to open up a new [github issue](https://github.com/nightcode/jmilter/issues) or simply drop me a line at <dmitry@nightcode.org>.


 [1]: http://oss.sonatype.org/service/local/artifact/maven/redirect?r=releases&g=org.nightcode&a=jmilter&v=LATEST
