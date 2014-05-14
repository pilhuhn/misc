package de.bsd.nettymc;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple client that receives messages via multicast
 * (in this case from Ganglia).
 *
 * For this to work, a gmond must be either running on the local
 * machine or its sender ttl must be set high enough. The default
 * of 1 will not work to receive data from a remote box.
 *
 * @author Heiko W. Rupp
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String GROUP = "239.2.11.71";
    private static final int PORT = 8649;

    public static void main(String[] args) throws Exception {
        Main main = new Main(args);
        main.run();
    }

    public Main(String[] args) {
    }

    private void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {

            // Obtain the interface for multicast reception
            // Should be the one that has the ip address that is
            // equivalent to the host name
            Inet4Address hostAddr = (Inet4Address) InetAddress.getLocalHost();
            NetworkInterface mcIf = NetworkInterface.getByInetAddress(hostAddr);

            // Try to set up an upd listener for Ganglia Messages
            InetSocketAddress gangliaSocket = new InetSocketAddress(GROUP, PORT);

            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                .group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.IP_MULTICAST_IF,mcIf)

                .localAddress(gangliaSocket)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(Channel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new SimpleChannelInboundHandler<DatagramPacket>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx,
                                                        DatagramPacket msg) throws Exception {
                                logger.info("Got a packet :-)");
                            }
                        });
                    }
                })
            ;

            ChannelFuture gangliaFuture = bootstrap.bind().sync();
            logger.info("Netty listening on udp " + gangliaFuture.channel().localAddress());
            DatagramChannel channel = (DatagramChannel) gangliaFuture.channel();

            channel.joinGroup(gangliaSocket, mcIf).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        logger.error("Join failed: " + future.cause());
                    }
                    else {
                        logger.info("Joined the group");
                    }
                }
            });
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

}
