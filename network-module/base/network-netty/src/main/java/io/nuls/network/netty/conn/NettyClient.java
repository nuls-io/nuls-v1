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

package io.nuls.network.netty.conn;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.nuls.core.tools.log.Log;
import io.nuls.network.model.Node;
import io.nuls.network.netty.conn.handler.ClientChannelHandler;
import io.nuls.network.netty.conn.initializer.NulsChannelInitializer;

import static io.nuls.network.constant.NetworkConstant.CONNETCI_TIME_OUT;


public class NettyClient {

    public static EventLoopGroup worker = new NioEventLoopGroup();

    private Bootstrap boot;

    private SocketChannel socketChannel;

    private Node node;

    public NettyClient(Node node) {
        this.node = node;
        boot = new Bootstrap();

        boot.attr(NodeAttributeKey.NODE_KEY, node);
        boot.group(worker)
                .channel(NioSocketChannel.class)
//                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.TCP_NODELAY, true)            //Send messages immediately
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_SNDBUF, 128 * 1024)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNETCI_TIME_OUT)
                .handler(new NulsChannelInitializer<>(new ClientChannelHandler()));
    }

    public boolean start() {
        try {
            ChannelFuture future = boot.connect(node.getIp(), node.getSeverPort());
            future.await();
            return future.isSuccess();
        } catch (Exception e) {
            //maybe time out or refused or something
            if (socketChannel != null) {
                socketChannel.close();
            }
            return false;
        }
    }

}
