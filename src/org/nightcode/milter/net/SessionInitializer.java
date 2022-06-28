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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.nightcode.milter.codec.Int32LenFrameDecoder;
import org.nightcode.milter.codec.Int32LenFrameEncoder;
import org.nightcode.milter.codec.MilterPacketDecoder;
import org.nightcode.milter.codec.MilterPacketEncoder;

import static org.nightcode.milter.util.Properties.getBoolean;
import static org.nightcode.milter.util.Properties.getString;

/**
 * Implementation of ChannelInitializer.
 */
public class SessionInitializer extends ChannelInitializer<Channel> {

  private final boolean loggingEnabled;
  private final String  logLevel;

  private final ChannelHandler responseHandler;

  public SessionInitializer(ChannelHandler responseHandler) {
    this.responseHandler = responseHandler;

    loggingEnabled = getBoolean("jmilter.netty.loggingEnabled", false);
    logLevel       = getString("jmilter.netty.logLevel", "INFO");
  }

  @Override protected void initChannel(Channel channel) {
    ChannelPipeline pipeline = channel.pipeline();

    if (loggingEnabled) {
      pipeline.addLast("logger", new LoggingHandler(MilterGatewayManager.class.getName(), LogLevel.valueOf(logLevel)));
    }

    pipeline.addLast("frameDecoder", new Int32LenFrameDecoder());
    pipeline.addLast("frameEncoder", new Int32LenFrameEncoder());

    pipeline.addLast("milterPacketDecoder", new MilterPacketDecoder());
    pipeline.addLast("milterPacketEncoder", new MilterPacketEncoder());

    pipeline.addLast("milterResponseHandler", responseHandler);
  }
}
