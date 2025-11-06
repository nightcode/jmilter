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

import java.net.SocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;

import org.nightcode.milter.util.Log;
import org.nightcode.milter.util.Throwables;

import static org.nightcode.milter.MilterOptions.NETTY_NUMBER_OF_THREADS;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;
import static org.nightcode.milter.util.Properties.getInt;

class SocketServerFactory implements ServerFactory<SocketAddress> {

  private final SocketAddress address;

  SocketServerFactory(SocketAddress address) {
    this.address = address;
  }

  @Override public ServerBootstrap create() {
    int nThreads = getInt(NETTY_NUMBER_OF_THREADS, 0);

    EventLoopGroup                 acceptorGroup      = null;
    EventLoopGroup                 workerGroup        = null;
    Class<? extends ServerChannel> serverChannelClass = null;
    try {
      acceptorGroup      = new EpollEventLoopGroup(1, namedThreadFactory("jmilter-" + address + "-acceptor-epoll"));
      workerGroup        = new EpollEventLoopGroup(nThreads, namedThreadFactory("jmilter-" + address + "-worker-epoll"));
      serverChannelClass = EpollServerDomainSocketChannel.class;
    } catch (Throwable ex) {
      Log.debug().log(getClass(), "unable to initialize domain EPOLL transport: {}", Throwables.getRootCause(ex).getMessage());
    }
    if (serverChannelClass == null) {
      try {
        acceptorGroup      = new KQueueEventLoopGroup(1, namedThreadFactory("jmilter-" + address + "-acceptor-kqueue"));
        workerGroup        = new KQueueEventLoopGroup(nThreads, namedThreadFactory("jmilter-" + address + "-worker-kqueue"));
        serverChannelClass = KQueueServerDomainSocketChannel.class;
      } catch (Throwable ex) {
        Log.debug().log(getClass(), "unable to initialize domain KQueue transport: {}", Throwables.getRootCause(ex).getMessage());
        throw Throwables.rethrow(ex);
      }
    }

    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap
        .group(acceptorGroup, workerGroup)
        .channel(serverChannelClass)
        .option(ChannelOption.SO_BACKLOG, 2048)
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .localAddress(address);

    return serverBootstrap;
  }

  @Override public SocketAddress localAddress() {
    return address;
  }
}
