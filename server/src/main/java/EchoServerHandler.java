

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private int count ;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("~~~~~~Server channelRead()"+ " - " + + ++count);
        ByteBuf in = (ByteBuf) msg;
        System.out.println(
                "Server received: " + in.toString(CharsetUtil.UTF_8));
        ctx.writeAndFlush(Unpooled.copiedBuffer("Server sends this 1 : " + System.currentTimeMillis(),
                CharsetUtil.UTF_8));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        Thread.sleep(5000);
        System.out.println("~~~~~~Server channelReadComplete()");
        ctx.writeAndFlush(Unpooled.copiedBuffer("Server sends this 2: " + System.currentTimeMillis(),
                CharsetUtil.UTF_8));
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
