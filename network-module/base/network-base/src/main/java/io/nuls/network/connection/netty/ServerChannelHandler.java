package io.nuls.network.connection.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.model.Block;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;
import io.nuls.network.service.NetworkService;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author Vivi
 */
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    private NetworkService networkService;

    @Autowired
    private NodeManager nodeManager;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.debug("---------------------- server channelRegistered ------------------------- " + nodeId);

        String remoteIP = channel.remoteAddress().getHostString();

        // 由于每个节点既是服务器，同时也会作为客户端去主动连接其他节点，
        // 为防止两个节点同时作为服务器一方相互连接，在这里做硬性规定，
        // 两个节点同时相互连接时，ip数字小的一方作为服务器，大的一方作为客户端
        Map<String, Node> nodeMap = networkService.getNodes();
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
                        networkService.removeNode(node.getId());
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
        Log.debug("---------------------- server channelActive ------------------------- " + nodeId);

        String channelId = ctx.channel().id().asLongText();
        NioChannelMap.add(channelId, channel);
        Node node = new Node(channel.remoteAddress().getHostString(), channel.remoteAddress().getPort(), Node.IN);
        node.setChannelId(channelId);
        node.setStatus(Node.CONNECT);
        boolean success = nodeManager.addConnNode(node);

        if (!success) {
            ctx.channel().close();
            return;
        }
        Block bestBlock = NulsContext.getInstance().getBestBlock();
        NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_SEVER_TYPE, networkService.getNetworkParam().getPort(),
                bestBlock.getHeader().getHeight(), bestBlock.getHeader().getHash());
        HandshakeMessage handshakeMessage = new HandshakeMessage(body);
        networkService.sendToNode(handshakeMessage, node, false);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.debug(" ---------------------- server channelInactive ------------------------- " + nodeId);

        String channelId = ctx.channel().id().asLongText();
        NioChannelMap.remove(channelId);
        Node node = networkService.getNode(nodeId);

        if (node != null) {
            if (channelId.equals(node.getChannelId())) {
//                System.out.println("------------ sever channelInactive remove node-------------" + node.getId());
                networkService.removeNode(nodeId);
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
        SocketChannel channel = (SocketChannel) ctx.channel();
        InetSocketAddress localAddress = channel.localAddress();
        InetSocketAddress remoteAddress = channel.remoteAddress();
        String local = IpUtil.getNodeId(localAddress);
        String remote = IpUtil.getNodeId(remoteAddress);
        Log.debug("--------------- ServerChannelHandler exceptionCaught :" + cause.getMessage()
                + ", localInfo: " + local + ", remoteInfo: " + remote);
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
//        Log.debug(" ---------------------- server channelRead ------------------------- " + nodeId);
        Node node = networkService.getNode(nodeId);
        if (node != null && node.isAlive()) {
            ByteBuf buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            buf.release();
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
//            getNetworkService().receiveMessage(buffer, node);
        }
    }

}
