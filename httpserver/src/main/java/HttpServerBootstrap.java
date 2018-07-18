import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;

public class HttpServerBootstrap {
    private final int port;

    public HttpServerBootstrap(int port) {
        this.port = port;
    }

    public static void main(String[] args)
            throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: " + HttpServerBootstrap.class.getSimpleName() +
                    " <port>"
            );
            return;
        }
        int port = Integer.parseInt(args[0]);
        new HttpServerBootstrap(port).start();
    }

    public void start() throws Exception {
        //socket  <--> head -------------------------- tail
        //socket --> execution starts --> 1st inbound handler --> 2nd inbound handler --->
        //socket <--      <-- 1st outbound handler <-- 2nd outbound handler <-- execution starts
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            io.netty.bootstrap.ServerBootstrap b = new io.netty.bootstrap.ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("decoder", new HttpRequestDecoder()); //1st inbound handler
                            ch.pipeline().addLast("encoder", new HttpResponseEncoder()); //1st outbound handler
                            ch.pipeline().addLast(new ServerReaderHandler()); //2nd inbound handler
                            //  ch.pipeline().addLast(new IdleStateHandler(0, 0, 5000));
                        }
                    });

            ChannelFuture f = b.bind().sync();
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture)
                        throws Exception {
                    if (channelFuture.isSuccess()) {
                        System.out.println(HttpServerBootstrap.class.getName() +
                                " started and listening for connections on " + channelFuture.channel().localAddress());
                    } else {
                        System.err.println("Bind attempt failed");
                        channelFuture.cause().printStackTrace();
                    }
                }
            });
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
