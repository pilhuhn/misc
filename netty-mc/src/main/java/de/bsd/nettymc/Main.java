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
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.oio.OioDatagramChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple client (proxy) that receives messages via multicast
 * @author Heiko W. Rupp
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String GROUP = "239.2.11.71";
    private static final int PORT = 8649;
    private static final String NETWORK_INTERFACE = "en5";

    public static void main(String[] args) throws Exception {
        Main main = new Main(args);
        main.run();
    }

    public Main(String[] args) {
    }

    private void run() throws Exception {
        EventLoopGroup oiogroup = new OioEventLoopGroup();
        try {

            // Try to set up an upd listener for Ganglia Messages
            // TODO there is something fishy still wrt reception of packets

/*
            tcpdump sees packets coming in to the MC-group and also with the
            right port:

            sh-3.2# tcpdump -i en5 dst 239.2.11.71
            tcpdump: verbose output suppressed, use -v or -vv for full protocol decode
            listening on en5, link-type EN10MB (Ethernet), capture size 65535 bytes
            09:06:12.262522 IP mackie.55850 > 239.2.11.71.8649: UDP, length 48
            09:06:12.262907 IP mackie.55850 > 239.2.11.71.8649: UDP, length 44

            Join seems to happen correctly:

            $ netstat -gsv
            en5:
            	inet 172.31.7.7
            	igmpv2 flags=0<>
            		group 239.2.11.71 mode exclude
            			mcast-macaddr 01:00:5e:02:0b:47

            I also see the membership report on start and leave reports on program exit
*/

            InetAddress groupAddress = InetAddress.getByName(GROUP);
            InetSocketAddress gangliaSocket = new InetSocketAddress(GROUP, PORT);
            InetSocketAddress localSocket = new InetSocketAddress("172.31.7.7",PORT);

            NetworkInterface mcIf = NetworkInterface.getByName(NETWORK_INTERFACE);  // TODO determine from main address

            Bootstrap bootstrap = new Bootstrap();
            InetSocketAddress remoteSocket = new InetSocketAddress("0.0.0.0",0);
            bootstrap
                .group(oiogroup)
                .channel(OioDatagramChannel.class)
//                .option(ChannelOption.IP_MULTICAST_IF, mcIf) // Needed at all? - want to receive on all IF
                .option(ChannelOption.SO_REUSEADDR, true)

//                .localAddress(gangliaSocket) // => Netty listening on udp /239.2.11.71:8649
//              .localAddress(8649) // => Netty listening on udp 0.0.0.0/0.0.0.0:8649 OR BindException: Address already in use
                .localAddress(localSocket) // => Netty listening on udp /172.31.7.7:8649

                .remoteAddress(remoteSocket) // Makes no difference
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(Channel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new SimpleChannelInboundHandler<DatagramChannel>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx,
                                                        DatagramChannel msg) throws Exception {
                                logger.info("Got a packet :-)");
                            }
                        });
                    }
                })
            ;

            logger.info("Bootstrap is " + bootstrap);
            ChannelFuture gangliaFuture = bootstrap.bind().sync();
            logger.info("Netty listening on udp " + gangliaFuture.channel().localAddress());
            DatagramChannel channel = (DatagramChannel) gangliaFuture.channel();

            // Next two lines makes no apparent difference which one to use
//            channel.joinGroup(gangliaSocket, mcIf).sync().addListener(new ChannelFutureListener() {
            channel.joinGroup(groupAddress).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        logger.error("Join failed: " + future.cause());
                    }
                }
            });
            logger.info("Joined the group");
            channel.closeFuture().sync();

        } finally {
            oiogroup.shutdownGracefully().sync();
        }
    }

}
