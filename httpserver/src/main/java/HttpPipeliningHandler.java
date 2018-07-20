import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import java.util.PriorityQueue;
import java.util.Queue;

public class HttpPipeliningHandler extends ChannelOutboundHandlerAdapter {

    static final int INITIAL_EVENTS_HELD = 3;
    private final int maxEventsHeld;
    private final Queue<HttpPipelinedResponse> holdingQueue;
    private int nextRequiredSequence = 1;

    public HttpPipeliningHandler(final int maxEventsHeld) {
        this.maxEventsHeld = maxEventsHeld;
        this.holdingQueue = new PriorityQueue<>(INITIAL_EVENTS_HELD);
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("Pipelining writer");

        if (msg instanceof HttpPipelinedResponse) {

            boolean channelShouldClose = false;

            synchronized (holdingQueue) {
                if (holdingQueue.size() < maxEventsHeld) {

                    final HttpPipelinedResponse currentResponse = (HttpPipelinedResponse) msg;
                    holdingQueue.add(currentResponse);

                    while (!holdingQueue.isEmpty()) {
                        final HttpPipelinedResponse queuedPipelinedResponse = holdingQueue.peek();
                        int currentSequenceNumber = queuedPipelinedResponse.getSequenceId();
                        if (currentSequenceNumber != nextRequiredSequence) {
                            break;
                        }
                        holdingQueue.remove();
                        ctx.write(queuedPipelinedResponse.getResponse());
                        nextRequiredSequence++;
                    }
                } else {
                    channelShouldClose = true;
                }
            }

            if (channelShouldClose) {
                ctx.close();
            }

        } else {
            super.write(ctx, msg, promise);
        }
        ReferenceCountUtil.release(msg);
    }
}
