package io.nuls.network.connection.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.nuls.core.tools.log.Log;

/**
 * @desription:
 * @author: PierreLuo
 * @date:
 */
public class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {  // 2
            IdleStateEvent event = (IdleStateEvent) evt;
            String type = "";
            if (event.state() == IdleState.READER_IDLE) {
                type = "read idle";
            } else if (event.state() == IdleState.WRITER_IDLE) {
                type = "write idle";
            } else if (event.state() == IdleState.ALL_IDLE) {
                type = "all idle";
            }
            SocketChannel channel = (SocketChannel) ctx.channel();
            Log.info(ctx.channel().remoteAddress() + "timeout type：" + type);
            Log.info(" ---------------------- HeartbeatServerHandler ---------------------- ");
            Log.info("localInfo: "+channel.localAddress().getHostString()+":" + channel.localAddress().getPort());
            Log.info("remoteInfo: "+channel.remoteAddress().getHostString()+":" + channel.remoteAddress().getPort());
            ctx.channel().close();

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
