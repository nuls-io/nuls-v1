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
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.network.model.Node;
import io.nuls.network.netty.manager.ConnectionManager;
import io.nuls.network.netty.message.MessageProcessor;

import java.io.IOException;

/**
 * @author Vivi
 */

@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private MessageProcessor messageProcessor = MessageProcessor.getInstance();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        ConnectionManager.getInstance().nodeHasConnectioned(socketChannel.remoteAddress().getHostString(), socketChannel.remoteAddress().getPort(), socketChannel);
    }

//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        super.channelInactive(ctx);
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            SocketChannel channel = (SocketChannel) ctx.channel();
            String nodeId = IpUtil.getNodeId(channel.remoteAddress());
            Attribute<Node> nodeAttribute = channel.attr(AttributeKey.valueOf("node-" + nodeId));

            Node node = nodeAttribute.get();
            ByteBuf buf = (ByteBuf) msg;

            messageProcessor.processor(buf, node);
        } catch (Exception e) {
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
        Attribute<Node> nodeAttribute = channel.attr(AttributeKey.valueOf("node-" + nodeId));

        Node node = nodeAttribute.get();
        if(node != null && node.getDisconnectListener() != null) {
            node.getDisconnectListener().action();
        }
        channel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.error("----------------- server exceptionCaught -------------------");

        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());

        if (!(cause instanceof IOException)) {
            Log.error("----------------nodeId:" + nodeId);
            Log.error(cause);
        }

        Attribute<Node> nodeAttribute = channel.attr(AttributeKey.valueOf("node-" + nodeId));

        Node node = nodeAttribute.get();
        if(node != null && node.getDisconnectListener() != null) {
            node.getDisconnectListener().action();
        }
        channel.close();
    }

}
