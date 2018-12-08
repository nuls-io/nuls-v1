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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;

import java.io.IOException;

/**
 * @author Vivi
 */

@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private NetworkParam networkParam = NetworkParam.getInstance();

    private static long severChannelRegister = 0;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();

        String remoteIP = channel.remoteAddress().getHostString();

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        SocketChannel socketChannel = (SocketChannel) ctx.channel();

        Node node = new Node(socketChannel.remoteAddress().getHostString(), socketChannel.remoteAddress().getPort(), Node.IN);
        node.setStatus(Node.CONNECT);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        try {
            channel.close();
            String nodeId = IpUtil.getNodeId(channel.remoteAddress());
            Log.info("close the channel of {} - channelInactive", nodeId);
        } catch (Exception e) {
            Log.error("close the channel is error {} - channelInactive", e.getMessage());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        try {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println(buf.readableBytes());
        } catch (Exception e) {
            Log.error(" ---------------------- server channelRead exception------------------------- " + nodeId);
            e.printStackTrace();
            throw e;
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.info(" ---------------------- server channelInactive ------------------------- " + nodeId);

        try {
            channel.close();
            Log.info("close the channel of {} - channelUnregistered", nodeId);
        } catch (Exception e) {
            Log.error("close the channel is error {} - channelUnregistered", e.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.error("----------------- server exceptionCaught -------------------");
        if (!(cause instanceof IOException)) {
            SocketChannel channel = (SocketChannel) ctx.channel();
            String nodeId = IpUtil.getNodeId(channel.remoteAddress());
            Log.error("----------------nodeId:" + nodeId);
            Log.error(cause);
        }
        ctx.channel().close();
    }

}
