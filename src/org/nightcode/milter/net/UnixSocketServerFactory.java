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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import org.nightcode.milter.util.Log;
import org.nightcode.milter.util.Throwables;

import static org.nightcode.milter.MilterOptions.NETTY_NUMBER_OF_THREADS;
import static org.nightcode.milter.MilterOptions.NETTY_SO_BACKLOG;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;
import static org.nightcode.milter.util.Properties.getInt;

class UnixSocketServerFactory implements ServerFactory<DomainSocketAddress> {

  private final DomainSocketAddress address;

  UnixSocketServerFactory(DomainSocketAddress address) {
    this.address = address;
  }

  @Override public ServerBootstrap create() {
    int nThreads = getInt(NETTY_NUMBER_OF_THREADS, 0);

    Supplier<IoHandlerFactory> factorySupplier;
    Class<? extends ServerChannel> channelClass;

    if (Epoll.isAvailable()) {
      factorySupplier = EpollIoHandler::newFactory;
      channelClass    = EpollServerDomainSocketChannel.class;
    } else if (KQueue.isAvailable()) {
      factorySupplier = KQueueIoHandler::newFactory;
      channelClass    = KQueueServerDomainSocketChannel.class;
    } else {
      throw new IllegalStateException("netty native transport (Epoll/KQueue) is required for Unix Domain Socket");
    }

    ThreadFactory acceptorTf = namedThreadFactory("jmilter-" + address + "-" + channelClass.getSimpleName() + "-acceptor");
    ThreadFactory workerTf   = namedThreadFactory("jmilter-" + address + "-" + channelClass.getSimpleName() + "-worker");

    Path socketPath = Paths.get(address.path());
    try {
      // clean up old socket file if it exists
      Files.deleteIfExists(socketPath);
    } catch (IOException ex) {
      Log.debug().log(getClass(), "unable to delete existing socket file: {}", socketPath);
      throw Throwables.rethrow(ex);
    }

    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap
        .group(new SingleThreadIoEventLoop(null, acceptorTf, factorySupplier.get())
            , new MultiThreadIoEventLoopGroup(nThreads, workerTf, factorySupplier.get()))
        .channel(channelClass)
        .option(ChannelOption.SO_BACKLOG, getInt(NETTY_SO_BACKLOG, 2048))
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .localAddress(address);

    return serverBootstrap;
  }

  @Override public DomainSocketAddress localAddress() {
    return address;
  }
}
