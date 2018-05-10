package io.nuls.network.test;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;
import io.nuls.core.tools.network.IpUtil;

import java.io.UnsupportedEncodingException;

@ChannelHandler.Sharable
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private AttributeKey<Node> key = AttributeKey.valueOf("node");

    public ClientChannelHandler() {

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--------------ClientHandler   channelRegistered---------------------");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--------------ClientHandler   channelActive---------------------");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--------------ClientHandler   channelInactive---------------------");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        System.out.println("--------------ClientHandler   channelRead---------------------");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        System.out.println("--------------ClientHandler   exceptionCaught---------------------");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--------------ClientHandler   channelUnregistered---------------------");
        SocketChannel channel = (SocketChannel) ctx.channel();
        NetworkTest.removeNode(IpUtil.getNodeId(channel.remoteAddress()));

    }
}
