package io.nuls.network.message.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.network.connection.netty.NioChannelMap;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.entity.NetworkEventResult;
import io.nuls.network.entity.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.BaseNetworkMessage;
import io.nuls.network.protocol.message.HandshakeMessage;

import io.netty.channel.socket.SocketChannel;
import io.nuls.network.protocol.message.NetworkMessageBody;

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
    public NetworkEventResult process(BaseNetworkMessage message, Node node) {

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

        node.setFailCount(0);
        node.setSeverPort(body.getSeverPort());
        nodeManager.saveNode(node);

        if (!isServer) {
//            Block bestBlock = NulsContext.getInstance().getBestBlock();
//            body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_CLIENT_TYPE, networkParam.getPort(),
//                    bestBlock.getHeader().getHeight(), bestBlock.getHeader().getHash());
            body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_CLIENT_TYPE, networkParam.getPort(),
                    10001, NulsDigestData.calcDigestData("a1b2c3d4e5gf6g7h8i9j10".getBytes()));
            return new NetworkEventResult(true, new HandshakeMessage(body));
        }
        return null;
    }
}
