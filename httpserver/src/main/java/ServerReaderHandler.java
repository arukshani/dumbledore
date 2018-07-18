import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ServerReaderHandler extends ChannelInboundHandlerAdapter {

    private int sequence = 0;
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("Server received request from client :" + ctx.channel().id());
        if (msg instanceof LastHttpContent) {
            sequence++;
            System.out.println("Last HTTP content received : " + sequence);
           /* ctx.channel().eventLoop().execute(() -> {
                    System.out.println("Current thread id : " + Thread.currentThread().getId());
                    });*/
            if (sequence == 1) {
                ScheduledFuture<?> future = executor.schedule(
                        new Runnable() {
                            @Override
                            public void run() {
                                ByteBuf content = Unpooled.copiedBuffer("server response" + 1, StandardCharsets.UTF_8);
                                final DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
                                httpResponse.headers().add(CONTENT_LENGTH, content.readableBytes());
                                httpResponse.headers().add("message-id", 1);
                                System.out.println("Now it is 10000ms later " + 1);
                                System.out.println("Starting to write server response : " + 1);
                                ctx.writeAndFlush(httpResponse);
                            }
                        }, 10000, TimeUnit.MILLISECONDS);
            }

            if (sequence == 2) {
                ScheduledFuture<?> future = executor.schedule(
                        new Runnable() {
                            @Override
                            public void run() {
                                ByteBuf content = Unpooled.copiedBuffer("server response" + 2, StandardCharsets.UTF_8);
                                final DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
                                httpResponse.headers().add(CONTENT_LENGTH, content.readableBytes());
                                httpResponse.headers().add("message-id", 2);
                                System.out.println("Now it is 5000ms later " + 2);
                                System.out.println("Starting to write server response : " + 2);
                                ctx.writeAndFlush(httpResponse);
                            }
                        }, 5000, TimeUnit.MILLISECONDS);
            }

            //TODO:Need to close the channel once both the responses have been sent out
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("Server finished reading request from client " + ctx.channel().id());
       // ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel active from server side " + ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Server channel inactive " + ctx.channel().id());
    }
}
