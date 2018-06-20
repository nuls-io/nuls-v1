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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.kernel.context.NulsContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.BroadcastHandler;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author Vivi
 */

@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private NodeManager nodeManager = NodeManager.getInstance();

    private NetworkParam networkParam = NetworkParam.getInstance();

    private BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    private ConnectionManager connectionManager = ConnectionManager.getInstance();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        System.out.println("---------------------- server channelRegistered ------------------------- " + nodeId);

        String remoteIP = channel.remoteAddress().getHostString();
        //查看是否是本机尝试连接本机地址 ，如果是直接关闭连接
        if (networkParam.getLocalIps().contains(remoteIP)) {
            System.out.println("----------------------本机尝试连接本机地址关闭 ------------------------- " + nodeId);
            ctx.channel().close();
            return;
        }

        // 由于每个节点既是服务器，同时也会作为客户端去主动连接其他节点，
        // 为防止两个节点同时作为服务器一方相互连接，当有发现相同ip的主动节点时，就关闭当前连接
        Map<String, Node> nodeMap = nodeManager.getConnectedNodes();
        for (Node node : nodeMap.values()) {
            if (node.getIp().equals(remoteIP)) {
                if (node.getType() == Node.OUT) {
                    System.out.println("--------------- 相同ip外网连接   -----------------" + nodeId);
                    ctx.channel().close();
                    return;
//
//                    String localIP = InetAddress.getLocalHost().getHostAddress();
//                    boolean isLocalServer = IpUtil.judgeLocalIsServer(localIP, remoteIP);
//                    //判断自己是否为服务器端
//                    if (!isLocalServer) {
//                        //不是则删除连接
//                        System.out.println("---------------    -----------------");
//                        ctx.channel().close();
//                        return;
//                    } else {
//                        //如果自己是服务器端，则删除当前主动作为客户端连接出去的节点，保存当前作为服务器端的连接
////                        System.out.println("----------------sever client register each other remove node-----------------" + node.getId());
//                        nodeManager.removeNode(node.getId());
//                    }
                }
            }
        }

        // if More than 10 in nodes of the same IP, close this channel
        // 如果相同ip的连接已经超过了10个，则不再接受
        int count = 0;
        for (Node n : nodeMap.values()) {
            if (n.getIp().equals(remoteIP)) {
                count++;
                if (count >= NetworkConstant.SAME_IP_MAX_COUNT) {
                    System.out.println("-------------超过10个-----" + nodeId);
                    ctx.channel().close();
                    return;
                }
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        System.out.println("---------------------- server channelActive ------------------------- " + nodeId);

        String channelId = ctx.channel().id().asLongText();
        NioChannelMap.add(channelId, channel);
        Node node = new Node(channel.remoteAddress().getHostString(), channel.remoteAddress().getPort(), Node.IN);
        node.setChannelId(channelId);
        node.setStatus(Node.CONNECT);
        boolean success = nodeManager.processConnectedNode(node);
        if (!success) {
            ctx.channel().close();
            return;
        }
        //握手成功时，告知对方其自身的外网ip地址
        NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_SEVER_TYPE, networkParam.getPort(),
                NulsContext.getInstance().getBestHeight(), NulsContext.getInstance().getBestBlock().getHeader().getHash(),
                channel.remoteAddress().getHostString());
        HandshakeMessage handshakeMessage = new HandshakeMessage(body);
        broadcastHandler.broadcastToNode(handshakeMessage, node, false);
    }

//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        super.channelInactive(ctx);
//
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("----------------- server exceptionCaught -------------------");
        cause.printStackTrace();
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        try {
            Node node = nodeManager.getNode(nodeId);
            if (node != null && node.isAlive()) {
                ByteBuf buf = (ByteBuf) msg;
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                buf.release();
                ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
                buffer.put(bytes);
                connectionManager.receiveMessage(buffer, node);
            }
        } catch (Exception e) {
            System.out.println(" ---------------------- server channelRead exception------------------------- " + nodeId);
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        System.out.println(" ---------------------- server channelInactive ------------------------- " + nodeId);

        String channelId = ctx.channel().id().asLongText();
        NioChannelMap.remove(channelId);
        Node node = nodeManager.getNode(nodeId);

        if (node != null) {
            if (channelId.equals(node.getChannelId())) {
                nodeManager.removeNode(nodeId);
            } else {
                System.out.println("--------------server channel id different----------------------");
                System.out.println("--------------server channel id different----------------------");
                System.out.println("--------------server channel id different----------------------");
                System.out.println("--------node:" + node.getId() + ",type:" + node.getType());
                System.out.println(node.getChannelId());
                System.out.println(channelId);
            }
        }
    }

}
