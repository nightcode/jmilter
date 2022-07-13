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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;

public class LocalServerFactory implements ServerFactory<LocalAddress> {

  private static final LocalAddress TEST_ADDRESS = new LocalAddress("test.id");

  @Override public ServerBootstrap create() {
    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap
        .group(new NioEventLoopGroup(1), new NioEventLoopGroup(1))
        .channel(LocalServerChannel.class)
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .localAddress(TEST_ADDRESS);
    return serverBootstrap;
  }

  @Override public LocalAddress localAddress() {
    return TEST_ADDRESS;
  }
}
