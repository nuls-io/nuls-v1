package io.nuls.network.service.impl.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {

    private int port;

    private ServerBootstrap serverBootstrap;
    private static EventLoopGroup boss;
    private static EventLoopGroup worker;

    public NettyServer(int port) {
        this.port = port;
    }

    public void init() {
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.TCP_NODELAY, true)            //Send messages immediately
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new NulsChannelInitializer<>(new ServerChannelHandler()));
    }

    public void start() throws InterruptedException {
        try {
            // Start the server.
            ChannelFuture future = serverBootstrap.bind(port).sync();
            // Wait until the server socket is closed.
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw e;
        } finally {
            // Shut down all event loops to terminate all threads.
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
