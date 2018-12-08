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
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.nuls.core.tools.log.Log;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;

import java.io.IOException;

public class ClientChannelHandler extends ChannelInboundHandlerAdapter {


    private AttributeKey<Node> key = AttributeKey.valueOf("node");

    private NetworkParam networkParam = NetworkParam.getInstance();

    public ClientChannelHandler() {

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        Attribute<Node> nodeAttribute = ctx.channel().attr(key);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        socketChannel.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println(buf.readableBytes());
        } catch (Exception e) {
            throw e;
        } finally {
           ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof IOException) && !(cause instanceof TooLongFrameException)) {
            Attribute<Node> nodeAttribute = ctx.channel().attr(key);
            Node node = nodeAttribute.get();
            Log.error("----------------nodeId:" + node.getId());
            Log.error(cause);

        }
        if (cause instanceof TooLongFrameException) {
            Attribute<Node> nodeAttribute = ctx.channel().attr(key);
            Node node = nodeAttribute.get();
            if (node != null) {
                node.setCanConnect(false);
            }
        }
        if (cause instanceof TooLongFrameException) {
            Attribute<Node> nodeAttribute = ctx.channel().attr(key);
            Node node = nodeAttribute.get();
            node.setCanConnect(false);
        }
        ctx.channel().close();
    }

}
