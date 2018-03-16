package io.nuls.network.service.impl.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.network.IpUtil;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.service.NetworkService;

import java.net.InetAddress;
import java.nio.ByteBuffer;

@ChannelHandler.Sharable
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private NetworkService networkService;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        SocketChannel socketChannel = (SocketChannel) ctx.channel();
        String remoteIP = socketChannel.remoteAddress().getHostString();
        Node node = getNetworkService().getNode(remoteIP);
        if (node != null) {
            if (node.getStatus() == Node.CONNECT) {
                ctx.channel().close();
                return;
            }
            //When nodes try to connect to each other but not connected, select one of the smaller IP addresses as the server
            if (node.getType() == Node.OUT) {
                String localIP = InetAddress.getLocalHost().getHostAddress();
                boolean isLocalServer = IpUtil.judgeIsLocalServer(localIP, remoteIP);

                if (!isLocalServer) {
                    ctx.channel().close();
                    return;
                } else {
                    getNetworkService().removeNode(remoteIP);
                }
            }
        }
        NodeGroup group = getNetworkService().getNodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
        if (group.size() > getNetworkService().getNetworkParam().maxInCount()) {
            ctx.channel().close();
            return;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.debug("----------------------server channelActive ------------------------- ");
        String channelId = ctx.channel().id().asLongText();
        SocketChannel channel = (SocketChannel) ctx.channel();
        NioChannelMap.add(channelId, channel);
        Node node = new Node(getNetworkService().getNetworkParam(), Node.IN, channel.remoteAddress().getHostString(), channel.remoteAddress().getPort(), channelId);
        node.setStatus(Node.CONNECT);
        getNetworkService().addNodeToGroup(NetworkConstant.NETWORK_NODE_IN_GROUP, node);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.debug("----------------------server channelInactive ------------------------- ");
        SocketChannel channel = (SocketChannel) ctx.channel();
        String channelId = ctx.channel().id().asLongText();
        NioChannelMap.remove(channelId);
        Node node = getNetworkService().getNode(channel.remoteAddress().getHostString());
        if (node != null && channelId.equals(node.getChannelId())) {
            getNetworkService().removeNode(channel.remoteAddress().getHostString());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //Log.error(cause);
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        Node node = getNetworkService().getNode(channel.remoteAddress().getHostString());
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
