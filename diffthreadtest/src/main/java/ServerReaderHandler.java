import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ServerReaderHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof LastHttpContent) {
            System.out.println("Server received request from client :" + ctx.channel().id());
            ByteBuf content = Unpooled.copiedBuffer("server response" + 1, StandardCharsets.UTF_8);
            final DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
            httpResponse.headers().add(CONTENT_LENGTH, content.readableBytes());
            httpResponse.headers().add("message-id", 1);
            TestObj testObj = new TestObj();
            ctx.writeAndFlush(testObj);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("Server finished reading request from client " + ctx.channel().id());
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
