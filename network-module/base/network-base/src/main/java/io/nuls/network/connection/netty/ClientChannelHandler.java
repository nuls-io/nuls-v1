package io.nuls.network.connection.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.network.entity.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.service.NetworkService;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private NetworkService networkService;
    @Autowired
    private NodeManager nodeManager;

    private AttributeKey<Node> key = AttributeKey.valueOf("node");

    public ClientChannelHandler() {

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        Attribute<Node> nodeAttribute = channel.attr(key);
        Node node = nodeAttribute.get();
        String nodeId = node == null ? null : node.getId();
        Log.debug("---------------------- client channelRegistered -----------" + nodeId);

        Map<String, Node> nodes = nodeManager.getNodes();
        //Map<String, Node> nodes = getNetworkService().getNodes();
        // If a node with the same IP already in nodes, as a out node, can not add anymore
        for (Node n : nodes.values()) {
            //both ip and port equals , it means the node is myself
            if (n.getIp().equals(node.getIp()) && !n.getPort().equals(node.getSeverPort())) {
                Log.debug("----------------------client: it already had a connection: " + n.getId() + " type:" + n.getType() + ", this connection: " + nodeId + "---------------------- ");
                ctx.channel().close();
                return;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.debug(" ---------------------- client channelActive ----------" + nodeId);
        Log.debug("localInfo: "+channel.localAddress().getHostString()+":" + channel.localAddress().getPort());

        Attribute<Node> nodeAttribute = channel.attr(key);
        Node node = nodeAttribute.get();

        try {
            NioChannelMap.add(channelId, channel);
            node.setChannelId(channelId);
            node.setStatus(Node.CONNECT);
//            boolean result = getNetworkService().addConnNode(node);
            boolean result = false;
            if(!result) {
                channel.close();
            }
        } catch (Exception e) {
            Log.info("client channelActive error: " + nodeId);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
        Log.debug(" ---------------------- client channelInactive ---------------------- " + nodeId);
        Log.debug("localInfo: "+channel.localAddress().getHostString()+":" + channel.localAddress().getPort());

        String channelId = ctx.channel().id().asLongText();
        NioChannelMap.remove(channelId);
        Node node = networkService.getNode(nodeId);
        if (node != null) {
            if (node.getChannelId() == null || channelId.equals(node.getChannelId())) {
//                System.out.println(  "---------------client channelInactive remove node----------------" + nodeId);
                networkService.removeNode(node.getId());
            } else {
                Log.debug("---------------- client channelId different----------------" + channelId + "," + node.getChannelId());
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String nodeId = IpUtil.getNodeId(channel.remoteAddress());
//        Log.debug(" ---------------------- client channelRead ---------------------- " + nodeId);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.debug("--------------- ClientChannelHandler exceptionCaught :" + cause.getMessage());
        ctx.channel().close();
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        Attribute<Node> nodeAttribute = channel.attr(key);
        Node node = nodeAttribute.get();
        if (!channel.isActive() && node != null) {
//            getNetworkService().deleteNode(node.getId());
        }
    }
}