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

package io.nuls.network.connection.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.kernel.context.NulsContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.BroadcastHandler;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NewNodeManager;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;

import java.io.IOException;
import java.util.Map;

/**
 * @author Vivi
 */

@ChannelHandler.Sharable
public class NewServerChannelHandler extends ChannelInboundHandlerAdapter {

    private NewNodeManager nodeManager = NewNodeManager.getInstance();

    private NetworkParam networkParam = NetworkParam.getInstance();

    private BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    private ConnectionManager connectionManager = ConnectionManager.getInstance();

    private static long severChannelRegister = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        Channel channel = ctx.channel();
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        if (!validate(socketChannel)) {
            channel.close();
            return;
        }



//        String channelId = ctx.channel().id().asLongText();
//        NioChannelMap.add(channelId, channel);
        Node node = new Node(socketChannel.remoteAddress().getHostString(), socketChannel.remoteAddress().getPort(), Node.IN);
        node.setStatus(Node.CONNECT);
        boolean success = nodeManager.processConnectedNode(node, channel);
        if (!success) {
            ctx.channel().close();
            return;
        }
        //握手成功时，告知对方其自身的外网ip地址
        NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_SEVER_TYPE, networkParam.getPort(),
                NulsContext.getInstance().getBestHeight(), NulsContext.getInstance().getBestBlock().getHeader().getHash(),
                socketChannel.remoteAddress().getHostString());
        HandshakeMessage handshakeMessage = new HandshakeMessage(body);
        broadcastHandler.broadcastToNode(handshakeMessage, node, false);
    }


    private boolean validate(SocketChannel channel) {
        //查看是否是本机尝试连接本机地址 ，如果是直接关闭连接
        String remoteIP = channel.remoteAddress().getHostString();
        if (networkParam.getLocalIps().contains(remoteIP)) {
            return false;
        }

        // 由于每个节点既是服务器，同时也会作为客户端去主动连接其他节点
        // 为防止两个节点同时作为服务器一方相互连接，当有发现相同ip的主动节点时，就关闭当前连接
        Node node = nodeManager.getNode(remoteIP);
        if (node.isAlive()) {
            return false;
        }

        // if More than 10 in nodes of the same IP, close this channel
        // 如果相同ip的连接已经超过了10个，则不再接受
        int count = 0;
        Map<String, Node> handerShakeNodeMap = nodeManager.getHanderShakeNode();
        for (Node n : handerShakeNodeMap.values()) {
            if (n.getIp().equals(remoteIP)) {
                count++;
                if (count >= NetworkConstant.SAME_IP_MAX_COUNT) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        try {
            Node node = nodeManager.getNode(nodeId);
            if (node != null && node.isAlive()) {
                ByteBuf buf = (ByteBuf) msg;
                connectionManager.receiveMessage(buf, node);
            } else {
                Log.error("-----------------server channelRead  node is null -----------------" + nodeId);
            }
        } catch (Exception e) {
//            System.out.println(" ---------------------- server channelRead exception------------------------- " + nodeId);
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
//        Log.info(" ---------------------- server channelInactive ------------------------- " + nodeId);

        Node node = nodeManager.getNode(nodeId);
        if (node != null) {
//            nodeManager.removeNode(nodeId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        Log.info("----------------- server exceptionCaught -------------------");
        if (!(cause instanceof IOException)) {
            SocketChannel channel = (SocketChannel) ctx.channel();
            String nodeId = IpUtil.getNodeId(channel.remoteAddress());
            Log.error("----------------nodeId:" + nodeId);
            Log.error(cause);
//            nodeManager.deleteNodeFromDB(nodeId);
//            return;
        }
        ctx.channel().close();
    }

}
