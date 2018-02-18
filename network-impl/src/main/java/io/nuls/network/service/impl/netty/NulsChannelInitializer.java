package io.nuls.network.service.impl.netty;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;

public class NulsChannelInitializer<T extends ChannelInboundHandlerAdapter> extends ChannelInitializer<SocketChannel> {

    private T t;

    public NulsChannelInitializer(T t) {
        this.t = t;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast("decoder", new LengthFieldBasedFrameDecoder(10 * 1024 * 1024, 0, 8, 0, 8));
        p.addLast("encoder0", new LengthFieldPrepender(8, false));
        p.addLast(t);
    }
}
