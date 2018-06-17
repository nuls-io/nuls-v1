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
import io.nuls.network.manager.NodeManager2;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;

import java.net.InetAddress;
import java.util.Map;

/**
 * @author Vivi
 */

@ChannelHandler.Sharable
public class ServerChannelHandler2 extends ChannelInboundHandlerAdapter {

    private NodeManager2 nodeManager = NodeManager2.getInstance();

    private NetworkParam networkParam = NetworkParam.getInstance();

    private BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    private ConnectionManager connectionManager = ConnectionManager.getInstance();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
//        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
//        Log.info("---------------------- server channelRegistered ------------------------- " + nodeId);
        System.out.println("----------------- server channelRegistered -------------------");
        // 由于每个节点既是服务器，同时也会作为客户端去主动连接其他节点，
        // 为防止两个节点同时作为服务器一方相互连接，在这里做硬性规定，
        // 两个节点同时相互连接时，ip数字小的一方作为服务器，大的一方作为客户端
        String remoteIP = channel.remoteAddress().getHostString();
        Map<String, Node> nodeMap = nodeManager.getNodes();
        for (Node node : nodeMap.values()) {
            if (node.getIp().equals(remoteIP)) {
                if (node.getType() == Node.OUT) {
                    String localIP = InetAddress.getLocalHost().getHostAddress();
                    boolean isLocalServer = IpUtil.judgeLocalIsServer(localIP, remoteIP);
                    //判断自己是否为服务器端
                    if (!isLocalServer) {
                        //不是则删除连接
                        System.out.println("---------------    -----------------");
                        ctx.channel().close();
                        return;
                    } else {
                        //如果自己是服务器端，则删除当前主动作为客户端连接出去的节点，保存当前作为服务器端的连接
//                        System.out.println("----------------sever client register each other remove node-----------------" + node.getId());
                        nodeManager.removeNode(node.getId());
                    }
                }
            }
        }
        nodeMap = nodeManager.getConnectedNodes();
        // if More than 10 in nodes of the same IP, close this channel
        // 如果相同ip的连接已经超过了10个，则不再接受
        int count = 0;
        for (Node n : nodeMap.values()) {
            if (n.getIp().equals(remoteIP)) {
                count++;
                if (count >= NetworkConstant.SAME_IP_MAX_COUNT) {
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
//        Log.info("---------------------- server channelActive ------------------------- " + nodeId);

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

        System.out.println("----------------- server channelActive -------------------");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
//        Log.info(" ---------------------- server channelInactive ------------------------- " + nodeId);

        String channelId = ctx.channel().id().asLongText();
        NioChannelMap.remove(channelId);
        Node node = nodeManager.getNode(nodeId);

        if (node != null) {
            if (channelId.equals(node.getChannelId())) {
//                System.out.println("------------ sever channelInactive remove node-------------" + node.getId());
                nodeManager.removeNode(nodeId);
            } else {
                Log.info("--------------server channel id different----------------------");
                Log.info("--------node:" + node.getId() + ",type:" + node.getType());
                Log.info(node.getChannelId());
                Log.info(channelId);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("----------------- server exceptionCaught -------------------");
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.out.println("----------------- server channelRead -------------------");
    }

}
