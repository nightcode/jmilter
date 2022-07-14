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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;

import static org.nightcode.milter.MilterOptions.NETTY_AUTO_READ;
import static org.nightcode.milter.MilterOptions.NETTY_CONNECT_TIMEOUT_MS;
import static org.nightcode.milter.util.Properties.getBoolean;
import static org.nightcode.milter.util.Properties.getInt;

public class LocalConnectionFactory implements ConnectionFactory<LocalAddress> {

  private static final LocalAddress TEST_ADDRESS = new LocalAddress("test.id");

  @Override public Bootstrap create() {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap
        .group(new NioEventLoopGroup(1))
        .channel(LocalChannel.class)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getInt(NETTY_CONNECT_TIMEOUT_MS, 5_000))
        .option(ChannelOption.AUTO_READ,              getBoolean(NETTY_AUTO_READ, true))
        .option(ChannelOption.ALLOCATOR,              PooledByteBufAllocator.DEFAULT)
        .remoteAddress(TEST_ADDRESS);
    return bootstrap;
  }

  @Override public LocalAddress remoteAddress() {
    return TEST_ADDRESS;
  }
}
