package io.nuls.network.message.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.network.connection.netty.NioChannelMap;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.HandshakeMessage;

import io.netty.channel.socket.SocketChannel;
import io.nuls.network.protocol.message.NetworkMessageBody;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.Map;

public class HandshakeMessageHandler implements BaseNetworkMeesageHandler {

    private NodeManager nodeManager = NodeManager.getInstance();

    private NetworkParam networkParam = NetworkParam.getInstance();

    private static HandshakeMessageHandler instance = new HandshakeMessageHandler();

    private HandshakeMessageHandler() {

    }

    public static HandshakeMessageHandler getInstance() {
        return instance;
    }

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {
        HandshakeMessage handshakeMessage = (HandshakeMessage) message;
        SocketChannel socketChannel = NioChannelMap.get(node.getChannelId());
        NetworkMessageBody body = handshakeMessage.getMsgBody();

        boolean isServer = false;
        boolean isSuccess = false;

        if (body.getHandshakeType() == NetworkConstant.HANDSHAKE_SEVER_TYPE) {
            isSuccess = nodeManager.handshakeNode(NetworkConstant.NETWORK_NODE_OUT_GROUP, node, body);
        } else {
            isServer = true;
            isSuccess = nodeManager.handshakeNode(NetworkConstant.NETWORK_NODE_IN_GROUP, node, body);
        }

        if (!isSuccess) {
            if (socketChannel != null) {
                Log.debug("localInfo: " + socketChannel.localAddress().getHostString() + ":" + socketChannel.localAddress().getPort());
                Log.debug("handshake failed, close the connetion.");
                socketChannel.close();
                return null;
            }
        }
        //握手成功时，更新自己的外网ip地址
      //  networkParam.getLocalIps().add(body.getNodeIp());
        node.setFailCount(0);
        node.setSeverPort(body.getSeverPort());
        node.setBestBlockHash(body.getBestBlockHash());
        node.setBestBlockHeight(body.getBestBlockHeight());
        nodeManager.saveNode(node);

        if (!isServer) {
            body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_CLIENT_TYPE, networkParam.getPort(),
                    NulsContext.getInstance().getBestHeight(), NulsContext.getInstance().getBestBlock().getHeader().getHash()
//                    socketChannel.remoteAddress().getHostString()
            );
            return new NetworkEventResult(true, new HandshakeMessage(body));
        }
        return null;
    }
}
