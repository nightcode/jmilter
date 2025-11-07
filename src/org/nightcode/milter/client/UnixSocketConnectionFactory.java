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
import org.nightcode.milter.util.Log;
import org.nightcode.milter.util.Throwables;


import static org.nightcode.milter.MilterOptions.NETTY_AUTO_READ;
import static org.nightcode.milter.MilterOptions.NETTY_CONNECT_TIMEOUT_MS;
import static org.nightcode.milter.MilterOptions.NETTY_NUMBER_OF_THREADS;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;
import static org.nightcode.milter.util.Properties.getBoolean;
import static org.nightcode.milter.util.Properties.getInt;

public class UnixSocketConnectionFactory implements ConnectionFactory<DomainSocketAddress> {

    private final DomainSocketAddress domainSocketAddress;

    public UnixSocketConnectionFactory(DomainSocketAddress domainSocketAddress) {
        this.domainSocketAddress = domainSocketAddress;
    }

    @Override
    public Bootstrap create() {
        int nThreads = getInt(NETTY_NUMBER_OF_THREADS, 0);

        EventLoopGroup workerGroup = null;
        Class<? extends Channel> channelClass = null;

        // 1. Select the appropriate native transport (Epoll or KQueue)
        try {
            if (Epoll.isAvailable()) { // **Linux (Epoll)**
                workerGroup = new EpollEventLoopGroup(nThreads,
                        namedThreadFactory("jmilter-uds-" + domainSocketAddress + "-worker-epoll"));
                channelClass = EpollDomainSocketChannel.class;
            } else if (KQueue.isAvailable()) { // **macOS/BSD (KQueue)**
                workerGroup = new KQueueEventLoopGroup(nThreads,
                        namedThreadFactory("jmilter-uds-" + domainSocketAddress + "-worker-kqueue"));
                channelClass = KQueueDomainSocketChannel.class;
            } else {
                // No native transport fallback available for UDS
                String errorMessage = "Netty native transport (Epoll/KQueue) is required for Unix Domain Sockets but is unavailable.";
                Log.error().log(getClass(), () -> errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        } catch (Throwable ex) {
            String errorMessage = "Failed to initialize native UDS transport: " + Throwables.getRootCause(ex).getMessage();
            Log.error().log(getClass(), () -> errorMessage);
            throw new IllegalStateException(errorMessage, ex);
        }

        // 2. Configure the Netty Bootstrap
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(workerGroup) // Client Bootstrap only needs a single group (worker)
                .channel(channelClass)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getInt(NETTY_CONNECT_TIMEOUT_MS, 5_000))
                .option(ChannelOption.AUTO_READ, getBoolean(NETTY_AUTO_READ, true))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .remoteAddress(domainSocketAddress);

        return bootstrap;
    }


    @Override
    public DomainSocketAddress remoteAddress() {
        return domainSocketAddress;
    }
}
