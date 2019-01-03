/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.network.netty.conn.initializer;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.nuls.network.netty.conn.handler.HeartbeatServerHandler;
import io.nuls.network.netty.conn.serialization.NulsMessageDecoder;
import io.nuls.network.netty.conn.serialization.NulsMessageEncoder;

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
        //p.addLast("decoder", new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, MAX_FRAME_LENGTH, 4, 4, 6, 0, true));
        //p.addLast("encoder0", new LengthFieldPrepender(8, false));
        p.addLast("decoder", new NulsMessageDecoder());
        p.addLast("encoder0", new NulsMessageEncoder());
        p.addLast("heartbeat", new HeartbeatServerHandler());
        p.addLast(t);
    }
}
