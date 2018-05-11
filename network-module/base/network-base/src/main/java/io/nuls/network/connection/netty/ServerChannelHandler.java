package io.nuls.network.connection.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.entity.Node;
import io.nuls.network.manager.BroadcastHandler;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;

import java.net.InetAddress;
import java.net.InetSocketAddress;
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
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.info("---------------------- server channelRegistered ------------------------- " + nodeId);

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
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.info("---------------------- server channelActive ------------------------- " + nodeId);

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
        //Block bestBlock = NulsContext.getInstance().getBestBlock();
//        NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_SEVER_TYPE, networkParam.getPort(),
//                bestBlock.getHeader().getHeight(), bestBlock.getHeader().getHash());
        NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_SEVER_TYPE, networkParam.getPort(),
                10001, new NulsDigestData("a1b2c3d4e5gf6g7h8i9j10".getBytes()));
                HandshakeMessage handshakeMessage = new HandshakeMessage(body);
        broadcastHandler.broadcastToNode(handshakeMessage, node, false);
        Log.info("---------------------- server channelActive END------------------------- " + nodeId);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.info(" ---------------------- server channelInactive ------------------------- " + nodeId);

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
        Log.info(" ---------------------- server channelInactive END------------------------- " + nodeId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.error("----------------ServerChannelHandler exceptionCaught-----------");
        Log.error(cause.getMessage());
//        SocketChannel channel = (SocketChannel) ctx.channel();
//        InetSocketAddress localAddress = channel.localAddress();
//        InetSocketAddress remoteAddress = channel.remoteAddress();
//        String local = IpUtil.getNodeId(localAddress);
//        String remote = IpUtil.getNodeId(remoteAddress);
//        Log.info("--------------- ServerChannelHandler exceptionCaught :" + cause.getMessage()
//                + ", localInfo: " + local + ", remoteInfo: " + remote);
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.info(" ---------------------- server channelRead ------------------------- " + nodeId);
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
        Log.info(" ---------------------- server channelRead END------------------------- " + nodeId);
    }

}
