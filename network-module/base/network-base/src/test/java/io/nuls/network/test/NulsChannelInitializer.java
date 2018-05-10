package io.nuls.network.test;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static io.nuls.network.constant.NetworkConstant.*;

public class NulsChannelInitializer<T extends ChannelInboundHandlerAdapter> extends ChannelInitializer<SocketChannel> {



    private T t;

    public NulsChannelInitializer(T t) {
        this.t = t;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast("idle", new IdleStateHandler(READ_IDEL_TIME_OUT, WRITE_IDEL_TIME_OUT, ALL_IDEL_TIME_OUT, TimeUnit.SECONDS));
        p.addLast("decoder", new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, 8, 0, 8));
        p.addLast("encoder0", new LengthFieldPrepender(8, false));
        p.addLast("heartbeat", new HeartbeatServerHandler());
        p.addLast(t);
    }
}
