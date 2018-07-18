import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;

@Sharable
public class ClientReaderHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Channel active from client side " + ctx.channel().id());
        HttpRequest request1 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test/outOfOrder");
        request1.headers().set(HttpHeaderNames.HOST, "localhost");
        request1.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request1.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request1.headers().set("message-id", "one");

        ctx.writeAndFlush(request1);

        HttpRequest request2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test/outOfOrder");
        request2.headers().set(HttpHeaderNames.HOST, "localhost");
        request2.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request2.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request2.headers().set("message-id", "two");

        ctx.writeAndFlush(request2);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("server response received from client: " + ctx.channel().id());
        if (msg instanceof HttpResponse) {
            HttpResponse receivedHeader = (HttpResponse) msg;
            System.out.println("Message id : " + receivedHeader.headers().get("message-id") + " Status:" + receivedHeader.status());
        } else {
            System.out.println("Read body from response");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client channel inactive: " + ctx.channel().id());
    }
}
