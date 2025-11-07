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
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDomainSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;

import static org.nightcode.milter.MilterOptions.NETTY_AUTO_READ;
import static org.nightcode.milter.MilterOptions.NETTY_CONNECT_TIMEOUT_MS;
import static org.nightcode.milter.MilterOptions.NETTY_NUMBER_OF_THREADS;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;
import static org.nightcode.milter.util.Properties.getBoolean;
import static org.nightcode.milter.util.Properties.getInt;

class UnixSocketConnectionFactory implements ConnectionFactory<DomainSocketAddress> {

  private final DomainSocketAddress address;

  UnixSocketConnectionFactory(DomainSocketAddress address) {
    this.address = address;
  }

  @Override public Bootstrap create() {
    int nThreads = getInt(NETTY_NUMBER_OF_THREADS, 0);

    EventLoopGroup           workerGroup;
    Class<? extends Channel> channelClass;

    if (Epoll.isAvailable()) { // Linux (Epoll)
      workerGroup  = new EpollEventLoopGroup(nThreads, namedThreadFactory("jmilter-" + address + "-worker-epoll"));
      channelClass = EpollDomainSocketChannel.class;
    } else if (KQueue.isAvailable()) { // macOS/BSD (KQueue)
      workerGroup  = new KQueueEventLoopGroup(nThreads, namedThreadFactory("jmilter-" + address + "-worker-kqueue"));
      channelClass = KQueueDomainSocketChannel.class;
    } else {
      throw new IllegalStateException("netty native transport (Epoll/KQueue) is required for Unix Domain Socket");
    }

    Bootstrap bootstrap = new Bootstrap();
    bootstrap
        .group(workerGroup)
        .channel(channelClass)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getInt(NETTY_CONNECT_TIMEOUT_MS, 5_000))
        .option(ChannelOption.AUTO_READ,              getBoolean(NETTY_AUTO_READ, true))
        .option(ChannelOption.ALLOCATOR,              PooledByteBufAllocator.DEFAULT)
        .remoteAddress(address);

    return bootstrap;
  }

  @Override public DomainSocketAddress remoteAddress() {
    return address;
  }
}
