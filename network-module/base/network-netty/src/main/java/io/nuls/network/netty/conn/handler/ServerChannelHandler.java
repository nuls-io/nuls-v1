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

package io.nuls.network.netty.conn.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
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
public class ServerChannelHandler extends SimpleChannelInboundHandler {

    private MessageProcessor messageProcessor = MessageProcessor.getInstance();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        boolean success = ConnectionManager.getInstance().nodeConnectIn(socketChannel);
        if (!success) {
            ctx.close();
        }
    }

    /**
     * 继承SimpleChannelInboundHandler后，只需要重新channelRead0方法，msg会自动释放
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Attribute<Node> nodeAttribute = channel.attr(AttributeKey.valueOf("node-" + nodeId));

        Node node = nodeAttribute.get();
        ByteBuf buf = (ByteBuf) msg;

        messageProcessor.processor(buf, node);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Attribute<Node> nodeAttribute = channel.attr(AttributeKey.valueOf("node-" + nodeId));

        Node node = nodeAttribute.get();
        if (node != null && node.getDisconnectListener() != null) {
            node.getDisconnectListener().action();
        }
        //channelUnregistered之前，channel就已经close了，可以调用channel.isOpen()查看状态
        //channel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.error("----------------- server exceptionCaught -------------------");
        if (!(cause instanceof IOException)) {
            SocketChannel channel = (SocketChannel) ctx.channel();
            String nodeId = IpUtil.getNodeId(channel.remoteAddress());
            //通常发生IOException是因为连接的节点断开了
            Log.error("----------------nodeId:" + nodeId);
            Log.error(cause);
        }

        //触发异常后，只需要关闭连接，就会执行channelUnregistered
//        Attribute<Node> nodeAttribute = channel.attr(AttributeKey.valueOf("node-" + nodeId));
//
//        Node node = nodeAttribute.get();
//        if (node != null && node.getDisconnectListener() != null) {
//            node.getDisconnectListener().action();
//        }
        ctx.close();
    }

}
