package io.nuls.network.service.impl.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;


public class NettyClient {

    public static EventLoopGroup worker = new NioEventLoopGroup();

    Bootstrap boot;

    private SocketChannel socketChannel;

    private Node node;

    private NetworkService networkService;

    public NettyClient(Node node) {
        this.node = node;
        boot = new Bootstrap();

        AttributeKey<Node> key = null;
        synchronized (NettyClient.class) {
            if (AttributeKey.exists("node")) {
                key = AttributeKey.valueOf("node");
            } else {
                key = AttributeKey.newInstance("node");
            }
        }
        boot.attr(key, node);

        boot.group(worker)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new NulsChannelInitializer<>(new ClientChannelHandler()));
    }

    public void start() {
        try {
            ChannelFuture future = boot.connect(node.getIp(), node.getSeverPort()).sync();
            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
            } else {
                System.out.println("---------------client start !success remove node-----------------" + node.getId());
                getNetworkService().removeNode(node.getId());
            }
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            Log.debug("-------------NettyClient start error: " + e.getMessage());
            //maybe time out or refused or something
            if (socketChannel != null) {
                socketChannel.close();
            }
            System.out.println("---------------client start exception remove node-----------------" + node.getId());
            getNetworkService().removeNode(node.getId());
        }
    }

    private NetworkService getNetworkService() {
        if (networkService == null) {
            networkService = NulsContext.getServiceBean(NetworkService.class);
        }
        return networkService;
    }
}
