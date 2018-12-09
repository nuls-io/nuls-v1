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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;
import io.nuls.core.tools.log.Log;
import io.nuls.network.model.Node;
import io.nuls.network.netty.conn.NodeAttributeKey;
import io.nuls.network.netty.message.MessageProcessor;

import java.io.IOException;

public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private MessageProcessor messageProcessor = MessageProcessor.getInstance();

    public ClientChannelHandler() {
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);

        Attribute<Node> nodeAttribute = ctx.channel().attr(NodeAttributeKey.NODE_KEY);

        Node node = nodeAttribute.get();
        if(node != null && node.getRegisterListener() != null) {
            node.getRegisterListener().action();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        Attribute<Node> nodeAttribute = ctx.channel().attr(NodeAttributeKey.NODE_KEY);

        Node node = nodeAttribute.get();
        if(node != null) {
            node.setChannel(ctx.channel());
        }
        if(node != null && node.getConnectedListener() != null) {
            node.getConnectedListener().action();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        socketChannel.close();
        Attribute<Node> nodeAttribute = ctx.channel().attr(NodeAttributeKey.NODE_KEY);

        Node node = nodeAttribute.get();

        if(node != null && node.getDisconnectListener() != null) {
            node.getDisconnectListener().action();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            Attribute<Node> nodeAttribute = ctx.channel().attr(NodeAttributeKey.NODE_KEY);
            Node node = nodeAttribute.get();
            ByteBuf buf = (ByteBuf) msg;

            messageProcessor.processor(buf, node);
        } catch (Exception e) {
            Log.error("----------------exceptionCaught   111 ---------");
            throw e;
        } finally {
           ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        Attribute<Node> nodeAttribute = ctx.channel().attr(NodeAttributeKey.NODE_KEY);

        if (!(cause instanceof IOException) && !(cause instanceof TooLongFrameException)) {
            Node node = nodeAttribute.get();
            Log.error("----------------nodeId:" + node.getId());
            Log.error(cause);

        }
        if (cause instanceof TooLongFrameException) {
            Node node = nodeAttribute.get();
            if (node != null) {
                node.setCanConnect(false);
            }
        }
        if (cause instanceof TooLongFrameException) {
            Node node = nodeAttribute.get();
            node.setCanConnect(false);
        }
        ctx.channel().close();

        Node node = nodeAttribute.get();

        if(node != null && node.getDisconnectListener() != null) {
            node.getDisconnectListener().action();
        }
    }

}
