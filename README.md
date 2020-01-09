# JMilter

[![Build Status](https://travis-ci.org/nightcode/jmilter.svg?branch=master)](https://travis-ci.org/nightcode/jmilter)
[![GitHub license](https://img.shields.io/github/license/nightcode/jmilter.svg)](https://github.com/nightcode/jmilter/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.nightcode/jmilter.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.nightcode%20AND%20a%3Ajmilter)

Java implementation of the Sendmail Milter protocol. The details of the Milter protocol can be found [here][1].
An introduction of using milter functionality with Postfix is available in the [Postfix before-queue Milter support][2] article.

How to use
----------

code

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
  MilterHandler milterHandler = new ExampleMilterHandler(milterActions, milterProtocolSteps);

  // gateway address
  String address = "0.0.0.0:4545";

  MilterGatewayManager gatewayManager = new MilterGatewayManager(address, milterHandler, ServiceManager.instance());

  gatewayManager.start();
```

The test folder contains the complete example code.

#### Available options

| Name                            | Possible values                 |
| ------------------------------- | ------------------------------- |
| jmilter.netty.transport         | NIO, EPOL, KQUEUE               |
| jmilter.netty.reconnectTimeoutMs| [0, Long.MAX_VALUE]             |
| jmilter.netty.loggingEnabled    | true, false                     |
| jmilter.netty.logLevel          | TRACE, DEBUG, INFO, WARN, ERROR |

Download
--------

Download [the latest jar][3] via Maven:
```xml
<dependency>
  <groupId>org.nightcode</groupId>
  <artifactId>jmilter</artifactId>
  <version>0.3</version>
</dependency>
```

Feedback is welcome. Please don't hesitate to open up a new [github issue](https://github.com/nightcode/jmilter/issues) or simply drop me a line at <dmitry@nightcode.org>.


 [1]: https://raw.githubusercontent.com/avar/sendmail-pmilter/master/doc/milter-protocol.txt
 [2]: http://www.postfix.org/MILTER_README.html
 [3]: https://oss.sonatype.org/service/local/artifact/maven/redirect?r=releases&g=org.nightcode&a=jmilter&v=LATEST
