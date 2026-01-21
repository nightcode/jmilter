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
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.SingleThreadIoEventLoop;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.nightcode.milter.util.Log;

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

    Supplier<IoHandlerFactory>     factorySupplier;
    Class<? extends ServerChannel> channelClass;

    if (Epoll.isAvailable()) {
      factorySupplier = EpollIoHandler::newFactory;
      channelClass    = EpollServerSocketChannel.class;
      Log.info().log(getClass(), "initialize netty EPOLL transport");
    } else if (KQueue.isAvailable()) {
      factorySupplier = KQueueIoHandler::newFactory;
      channelClass    = KQueueServerSocketChannel.class;
      Log.info().log(getClass(), "initialize netty KQUEUE transport");
    } else {
      factorySupplier = NioIoHandler::newFactory;
      channelClass    = NioServerSocketChannel.class;
    }

    ThreadFactory acceptorTf = namedThreadFactory("jmilter-" + address + "-" + channelClass.getSimpleName() + "-acceptor");
    ThreadFactory workerTf   = namedThreadFactory("jmilter-" + address + "-" + channelClass.getSimpleName() + "-worker");

    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap
        .group(new SingleThreadIoEventLoop(null, acceptorTf, factorySupplier.get())
            , new MultiThreadIoEventLoopGroup(nThreads, workerTf, factorySupplier.get()))
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
