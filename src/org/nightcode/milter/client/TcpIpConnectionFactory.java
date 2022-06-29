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

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.nightcode.milter.util.Log;
import org.nightcode.milter.util.Throwables;

import static org.nightcode.milter.MilterOptions.NETTY_AUTO_READ;
import static org.nightcode.milter.MilterOptions.NETTY_CONNECT_TIMEOUT_MS;
import static org.nightcode.milter.MilterOptions.NETTY_KEEP_ALIVE;
import static org.nightcode.milter.MilterOptions.NETTY_NUMBER_OF_THREADS;
import static org.nightcode.milter.MilterOptions.NETTY_REUSE_ADDRESS;
import static org.nightcode.milter.MilterOptions.NETTY_TCP_NO_DELAY;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;
import static org.nightcode.milter.util.Properties.getBoolean;
import static org.nightcode.milter.util.Properties.getInt;

class TcpIpConnectionFactory implements ConnectionFactory<InetSocketAddress> {

  private final InetSocketAddress address;

  TcpIpConnectionFactory(InetSocketAddress address) {
    this.address = address;
  }

  @Override public Bootstrap newConnection() {
    int nThreads = getInt(NETTY_NUMBER_OF_THREADS, 0);

    EventLoopGroup           workerGroup  = null;
    Class<? extends Channel> channelClass = null;
    try {
      workerGroup  = new EpollEventLoopGroup(nThreads, namedThreadFactory("jmilter-TcpIpConnection-epoll"));
      channelClass = EpollSocketChannel.class;
    } catch (Throwable ex) {
      Log.info().log(getClass()
          , () -> "unable to initialize netty EPOLL transport, switch to NIO: " + Throwables.getRootCause(ex).getMessage());
    }
    if (channelClass == null) {
      workerGroup  = new NioEventLoopGroup(nThreads, namedThreadFactory("jmilter-TcpIpConnection-nio"));
      channelClass = NioSocketChannel.class;
    }

    Bootstrap bootstrap = new Bootstrap();
    bootstrap
        .group(workerGroup)
        .channel(channelClass)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getInt(NETTY_CONNECT_TIMEOUT_MS, 5_000))
        .option(ChannelOption.AUTO_READ,              getBoolean(NETTY_AUTO_READ, true))
        .option(ChannelOption.SO_KEEPALIVE,           getBoolean(NETTY_KEEP_ALIVE, true))
        .option(ChannelOption.TCP_NODELAY,            getBoolean(NETTY_TCP_NO_DELAY, true))
        .option(ChannelOption.SO_REUSEADDR,           getBoolean(NETTY_REUSE_ADDRESS, true))
        .option(ChannelOption.ALLOCATOR,              PooledByteBufAllocator.DEFAULT)
        .remoteAddress(address);

    return bootstrap;
  }

  @Override public InetSocketAddress remoteAddress() {
    return address;
  }
}
