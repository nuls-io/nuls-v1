package io.nuls.network.service.impl.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.network.IpUtil;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.message.entity.GetVersionEvent;
import io.nuls.network.message.entity.HandshakeEvent;
import io.nuls.network.service.NetworkService;

import java.net.InetAddress;
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
        System.out.println("---------------------- server channelRegistered ------------------------- " + nodeId);

        String remoteIP = channel.remoteAddress().getHostString();
        Map<String, Node> nodeMap = getNetworkService().getNodes();
        for (Node node : nodeMap.values()) {
            if (node.getIp().equals(remoteIP)) {
                // 当前节点存在已主动连接出去的node，则只保留一个连接
                if (node.getType() == Node.OUT) {
                    String localIP = InetAddress.getLocalHost().getHostAddress();
                    boolean isLocalServer = IpUtil.judgeIsLocalServer(localIP, remoteIP);
                    if (!isLocalServer) {
                        // 关闭当前被动连接
                        ctx.channel().close();
                        return;
                    } else {
                        // 关闭已有主动连接
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
        System.out.println("---------------------- server channelActive ------------------------- " + nodeId);

        String channelId = ctx.channel().id().asLongText();
        NioChannelMap.add(channelId, channel);
        Node node = new Node(channel.remoteAddress().getHostString(), channel.remoteAddress().getPort(), Node.IN);
        node.setChannelId(channelId);
        node.setStatus(Node.CONNECT);
        boolean success = getNetworkService().addConnNode(node);
        if (!success) {
            ctx.channel().close();
            return;
        }
        HandshakeEvent event = new HandshakeEvent(NetworkConstant.HANDSHAKE_SEVER_TYPE);
        getNetworkService().sendToNode(event, nodeId, false);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        System.out.println(" ---------------------- server channelInactive ------------------------- " + nodeId);

        String channelId = ctx.channel().id().asLongText();
        NioChannelMap.remove(channelId);
        Node node = getNetworkService().getNode(nodeId);
        if (node != null) {
            if (channelId.equals(node.getChannelId())) {
                getNetworkService().removeNode(nodeId);
            } else {
                System.out.println("--------------channel id different----------------------");
                System.out.println(node.getChannelId());
                System.out.println(channelId);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.debug("--------------- ServerChannelHandler exceptionCaught :" + cause.getMessage(), cause);
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
//        System.out.println(" ---------------------- server channelRead ------------------------- " + nodeId);
        Node node = getNetworkService().getNode(nodeId);
        if (node != null && node.isAlive()) {
            ByteBuf buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            buf.release();
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            getNetworkService().receiveMessage(buffer, node);
        }
    }

    private NetworkService getNetworkService() {
        if (networkService == null) {
            networkService = NulsContext.getServiceBean(NetworkService.class);
        }
        return networkService;
    }
}
