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

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.nightcode.milter.util.Log;
import org.nightcode.milter.util.Throwables;

import static org.nightcode.milter.MilterOptions.NETTY_KEEP_ALIVE;
import static org.nightcode.milter.MilterOptions.NETTY_NUMBER_OF_THREADS;
import static org.nightcode.milter.MilterOptions.NETTY_REUSE_ADDRESS;
import static org.nightcode.milter.MilterOptions.NETTY_SO_BACKLOG;
import static org.nightcode.milter.MilterOptions.NETTY_TCP_NO_DELAY;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;
import static org.nightcode.milter.util.Properties.getBoolean;
import static org.nightcode.milter.util.Properties.getInt;

class TcpIpServerFactory implements ServerFactory<InetSocketAddress> {

  private final InetSocketAddress address;

  TcpIpServerFactory(InetSocketAddress address) {
    this.address = address;
  }

  @Override public ServerBootstrap create() {
    int nThreads = getInt(NETTY_NUMBER_OF_THREADS, 0);

    EventLoopGroup                 acceptorGroup = null;
    EventLoopGroup                 workerGroup   = null;
    Class<? extends ServerChannel> channelClass  = null;
    try {
      acceptorGroup = new EpollEventLoopGroup(1, namedThreadFactory("jmilter-" + address + "-acceptor-epoll"));
      workerGroup   = new EpollEventLoopGroup(nThreads, namedThreadFactory("jmilter-" + address + "-worker-epoll"));
      channelClass  = EpollServerSocketChannel.class;
    } catch (Throwable ex) {
      Log.info().log(getClass()
          , () -> "unable to initialize netty EPOLL transport, switch to NIO: " + Throwables.getRootCause(ex).getMessage());
    }
    if (channelClass == null) {
      acceptorGroup = new NioEventLoopGroup(1, namedThreadFactory("jmilter-" + address + "-acceptor-nio"));
      workerGroup   = new NioEventLoopGroup(nThreads, namedThreadFactory("jmilter-" + address + "-worker-nio"));
      channelClass  = NioServerSocketChannel.class;
    }

    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap
        .group(acceptorGroup, workerGroup)
        .channel(channelClass)
        .option(ChannelOption.SO_BACKLOG,   getInt(NETTY_SO_BACKLOG, 128))
        .option(ChannelOption.SO_REUSEADDR, getBoolean(NETTY_REUSE_ADDRESS, true))
        .childOption(ChannelOption.SO_KEEPALIVE, getBoolean(NETTY_KEEP_ALIVE, true))
        .childOption(ChannelOption.SO_REUSEADDR, getBoolean(NETTY_REUSE_ADDRESS, true))
        .childOption(ChannelOption.TCP_NODELAY,  getBoolean(NETTY_TCP_NO_DELAY, true))
        .childOption(ChannelOption.ALLOCATOR,    PooledByteBufAllocator.DEFAULT)
        .localAddress(address);

    return serverBootstrap;
  }

  @Override public InetSocketAddress localAddress() {
    return address;
  }
}
