package org.nightcode.milter.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDomainSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.DomainSocketChannel;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.nightcode.milter.AbstractMilterHandler;
import org.nightcode.milter.Actions;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.MilterHandler;
import org.nightcode.milter.ProtocolSteps;
import org.nightcode.milter.client.UnixSocketConnectionFactory;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.nightcode.milter.CommandCode.SMFIC_OPTNEG;
import static org.nightcode.milter.util.ExecutorUtils.namedThreadFactory;

public class UnixSocketConnectionFactoryTest {

    private static final String SOCKET_PATH = "target/jmilter-test.sock";
    private static final DomainSocketAddress TEST_ADDRESS = new DomainSocketAddress(SOCKET_PATH);

    private EventLoopGroup clientGroup;

    @Before
    public void setUp() {
        // Skip test if native transport not available
        Assume.assumeTrue(Epoll.isAvailable() || KQueue.isAvailable());
    }

    @After
    public void tearDown() throws Exception {
        if (clientGroup != null) {
            clientGroup.shutdownGracefully(0, 5, SECONDS).await();
        }
        // Clean up socket file so subsequent tests aren’t blocked
        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(SOCKET_PATH));
    }

    @Test
    public void testUnixDomainSocketConnectionFactory_connectAndTriggerOptneg() throws Exception {
        final CountDownLatch negotiateLatch = new CountDownLatch(1);

        MilterHandler milterHandler = new AbstractMilterHandler(Actions.DEF_ACTIONS, ProtocolSteps.DEF_PROTOCOL_STEPS) {
            @Override
            public void optneg(MilterContext context, int mtaProtocolVersion, Actions mtaActions,
                               ProtocolSteps mtaProtocolSteps) throws MilterException {
                super.optneg(context, mtaProtocolVersion, mtaActions, mtaProtocolSteps);
                negotiateLatch.countDown();
            }

            @Override
            public void quit(MilterContext context) {
                // no-op
            }
        };

        // Server side: bind to domain socket address using your ServerFactory
        ServerFactory<DomainSocketAddress> serverFactory = ServerFactory.unixSocketFactory(TEST_ADDRESS);

        try (MilterGatewayManager<DomainSocketAddress> gatewayManager =
                     new MilterGatewayManager<>(serverFactory, milterHandler)) {
            // Bind server
            gatewayManager.bind().get(500, SECONDS);

            // Client side: select event loop & channel class
            Class<? extends DomainSocketChannel> channelClass;
            if (KQueue.isAvailable()) {
                clientGroup = new KQueueEventLoopGroup(1, namedThreadFactory("jmilter-uds-client-kqueue"));
                channelClass = KQueueDomainSocketChannel.class;
            } else {
                clientGroup = new EpollEventLoopGroup(1, namedThreadFactory("jmilter-uds-client-epoll"));
                channelClass = EpollDomainSocketChannel.class;
            }

            // Use your UnixSocketConnectionFactory to build Bootstrap
            UnixSocketConnectionFactory factory = new UnixSocketConnectionFactory(TEST_ADDRESS);
            Bootstrap clientBootstrap = factory.create();
            clientBootstrap
                    .handler(new SessionInitializer(() ->
                            new io.netty.channel.SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                    // no-op
                                }
                            }));

            // Connect
            ChannelFuture cf = clientBootstrap.connect(TEST_ADDRESS).sync();
            assertTrue("Client connect failed", cf.isSuccess());

            Channel clientChannel = cf.channel();
            // Write the OPTNEG message (same as in your protocol tests)
            byte[] bytes = new byte[]{
                    (byte) SMFIC_OPTNEG.code(),
                    0x00, 0x00, 0x00, 0x06,
                    0x00, 0x00, 0x01, (byte) 0xFF,
                    0x00, 0x1F, (byte) 0xFF, (byte) 0xFF
            };
            clientChannel.writeAndFlush(Unpooled.copiedBuffer(bytes));

            boolean reached = negotiateLatch.await(5, SECONDS);
            assertTrue("Server did not receive optneg from client", reached);

            clientChannel.close().sync();
        }
    }
}