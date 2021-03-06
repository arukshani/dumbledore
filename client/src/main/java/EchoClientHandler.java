

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

@Sharable
public class EchoClientHandler
        extends SimpleChannelInboundHandler<ByteBuf> {

    private int count ;
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("~~~~~~Client channelActive()");
        ctx.writeAndFlush(Unpooled.copiedBuffer("Client sends this : " + System.currentTimeMillis(),
                CharsetUtil.UTF_8));

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        System.out.println("~~~~~~Client channelRead0()" + " - " + + ++count);
        System.out.println(
                "Client received: " + in.toString(CharsetUtil.UTF_8) + "  " + System.currentTimeMillis());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
