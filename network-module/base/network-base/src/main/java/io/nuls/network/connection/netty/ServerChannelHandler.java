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
import io.nuls.network.service.NetworkService;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author Vive
 */
@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    private NetworkService networkService;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.debug("---------------------- server channelRegistered ------------------------- " + nodeId);

        String remoteIP = channel.remoteAddress().getHostString();
        Map<String, Node> nodeMap = null;
                //getNetworkService().getNodes();
        for (Node node : nodeMap.values()) {
            if (node.getIp().equals(remoteIP)) {
                if (node.getType() == Node.OUT) {
                    String localIP = InetAddress.getLocalHost().getHostAddress();
                    boolean isLocalServer = IpUtil.judgeIsLocalServer(localIP, remoteIP);
                    if (!isLocalServer) {
                        ctx.channel().close();
                        return;
                    } else {
//                        System.out.println("----------------sever client register each other remove node-----------------" + node.getId());
                        getNetworkService().removeNode(node.getId());
                    }
                }
            }
        }

        // if has a node with same ip, and it's a out node, close this channel
        // if More than 10 in nodes of the same IP, close this channel
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
//        boolean success = getNetworkService().addConnNode(node);
        boolean success = false;
        if (!success) {
            ctx.channel().close();
            return;
        }
        Block bestBlock = NulsContext.getInstance().getBestBlock();
//        HandshakeEvent event = new HandshakeEvent(NetworkConstant.HANDSHAKE_SEVER_TYPE, getNetworkService().getNetworkParam().getPort(),
//                bestBlock.getHeader().getHeight(), bestBlock.getHeader().getHash().getDigestHex());
//        getNetworkService().sendToNode(event, nodeId, false);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.debug(" ---------------------- server channelInactive ------------------------- " + nodeId);

        String channelId = ctx.channel().id().asLongText();
        NioChannelMap.remove(channelId);
        Node node = getNetworkService().getNode(nodeId);
        if (node != null) {
            if (channelId.equals(node.getChannelId())) {
//                System.out.println("------------ sever channelInactive remove node-------------" + node.getId());
                getNetworkService().removeNode(nodeId);
            } else {
                Log.debug("--------------channel id different----------------------");
                Log.debug(node.getChannelId());
                Log.debug(channelId);
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
        Node node = getNetworkService().getNode(nodeId);
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

    private NetworkService getNetworkService() {
        if (networkService == null) {
            networkService = NulsContext.getServiceBean(NetworkService.class);
        }
        return networkService;
    }
}
