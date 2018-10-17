import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Created by rukshani on 10/16/18.
 */
public class ThirdOutHandler extends ChannelOutboundHandlerAdapter {
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("Third Out Handler: " + Thread.currentThread().getName()+ " : " + System.currentTimeMillis());
        ctx.writeAndFlush(msg);
    }
}
