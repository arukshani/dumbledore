import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class FirstOutHandler extends ChannelOutboundHandlerAdapter {

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("First Out Handler: " + Thread.currentThread().getName() + " : " + System.currentTimeMillis());
        ctx.writeAndFlush(msg);
    }
}
