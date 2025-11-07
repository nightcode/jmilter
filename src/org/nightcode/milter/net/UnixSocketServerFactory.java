package org.nightcode.milter.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import org.nightcode.milter.util.Log;
import org.nightcode.milter.util.Throwables;

import java.io.File;

import static org.nightcode.milter.MilterOptions.NETTY_NUMBER_OF_THREADS;
import static org.nightcode.milter.MilterOptions.NETTY_SO_BACKLOG;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;
import static org.nightcode.milter.util.Properties.getInt;

public class UnixSocketServerFactory implements ServerFactory<DomainSocketAddress> {

    private final DomainSocketAddress domainSocketAddress;

    public UnixSocketServerFactory(DomainSocketAddress domainSocketAddress) {
        this.domainSocketAddress = domainSocketAddress;
    }

    @Override
    public ServerBootstrap create() {
        int nThreads = getInt(NETTY_NUMBER_OF_THREADS, 0);

        EventLoopGroup acceptorGroup = null;
        EventLoopGroup workerGroup = null;
        Class<? extends ServerChannel> channelClass = null;

        try {
            if (Epoll.isAvailable()) { // **Linux (Epoll)**
                acceptorGroup = new EpollEventLoopGroup(1, namedThreadFactory("jmilter-" + domainSocketAddress + "-acceptor-epoll"));
                workerGroup = new EpollEventLoopGroup(nThreads, namedThreadFactory("jmilter-" + domainSocketAddress + "-worker-epoll"));
                channelClass = EpollServerDomainSocketChannel.class;
            } else if (KQueue.isAvailable()) { // **macOS/BSD (KQueue)**
                acceptorGroup = new KQueueEventLoopGroup(1, namedThreadFactory("jmilter-" + domainSocketAddress + "-acceptor-kqueue"));
                workerGroup = new KQueueEventLoopGroup(nThreads, namedThreadFactory("jmilter-" + domainSocketAddress + "-worker-kqueue"));
                channelClass = KQueueServerDomainSocketChannel.class;
            } else {
                // Netty has NO pure-Java fallback for Domain Sockets (unlike TCP/IP),
                // so we must throw an exception if native transport is missing.
                String errorMessage = "Netty native transport (Epoll/KQueue) is required for Unix Domain Sockets but is unavailable.";
                Log.error().log(getClass(), () -> errorMessage);
                throw new IllegalStateException(errorMessage);
            }

        } catch (Throwable ex) {
            String errorMessage = "Failed to initialize native UDS transport: " + Throwables.getRootCause(ex).getMessage();
            Log.error().log(getClass(), () -> errorMessage);
            throw new IllegalStateException(errorMessage, ex);
        }

        // Clean up old socket file if it exists
        File socketFile = new File(domainSocketAddress.path());
        if (socketFile.exists() && !socketFile.delete()) {
            throw new IllegalStateException("Cannot delete existing socket file: " + socketFile.getAbsolutePath());
        }

        // 2. Configure the ServerBootstrap
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(acceptorGroup, workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_BACKLOG, getInt(NETTY_SO_BACKLOG, 128))
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .localAddress(domainSocketAddress);

        return serverBootstrap;
    }

    @Override
    public DomainSocketAddress localAddress() {
        return domainSocketAddress;
    }
}
