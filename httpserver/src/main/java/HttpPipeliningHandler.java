import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class HttpPipeliningHandler extends ChannelOutboundHandlerAdapter{

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("Pipelining writer");
        ctx.write(msg);
    }
}
