/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.network.netty.conn.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @desription:
 * @author: PierreLuo
 */
public class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {  // 2
//            IdleStateEvent event = (IdleStateEvent) evt;
//            String type = "";
//            if (event.state() == IdleState.READER_IDLE) {
//                type = "read idle";
//            } else if (event.state() == IdleState.WRITER_IDLE) {
//                type = "write idle";
//            } else if (event.state() == IdleState.ALL_IDLE) {
//                type = "all idle";
//            }
//            SocketChannel channel = (SocketChannel) ctx.channel();
//            Log.info(ctx.c                                                                                                                                                                                                                                                                                                                                                                                               ``````hannel().remoteAddress() + "timeout type：" + type);
//            Log.info(" ---------------------- HeartbeatServerHandler ---------------------- ");
//            Log.info("localInfo: "+channel.localAddress().getHostString()+":" + channel.localAddress().getPort());
//            Log.info("remoteInfo: "+channel.remoteAddress().getHostString()+":" + channel.remoteAddress().getPort());
            ctx.channel().close();

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
