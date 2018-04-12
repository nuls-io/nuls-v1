package io.nuls.network.message.impl;

import io.netty.channel.socket.SocketChannel;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.utils.log.Log;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.HandshakeEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.NetworkService;
import io.nuls.network.service.impl.netty.NioChannelMap;

public class HandshakeEventHandler implements NetWorkEventHandler {

    private static HandshakeEventHandler instance = new HandshakeEventHandler();

    private NetworkService networkService;

    private HandshakeEventHandler() {
    }

    public static HandshakeEventHandler getInstance() {
        return instance;
    }

    @Override
    public NetworkEventResult process(BaseEvent event, Node node) {
        HandshakeEvent handshakeEvent = (HandshakeEvent) event;
        /**
         * 1 server receive the msg from client for handshake
         * 2 client receive the msg from server for handshake
         */
        SocketChannel socketChannel = NioChannelMap.get(node.getChannelId());

        boolean isServer = false;

        boolean isSuccess = false;

        if (handshakeEvent.getHandshakeType() == NetworkConstant.HANDSHAKE_SEVER_TYPE) {
            isSuccess = getNetworkService().handshakeNode(NetworkConstant.NETWORK_NODE_OUT_GROUP, node);
        } else {
            isServer = true;
            isSuccess = getNetworkService().handshakeNode(NetworkConstant.NETWORK_NODE_IN_GROUP, node);
        }

        if (!isSuccess) {
            Log.debug("localInfo: "+socketChannel.localAddress().getHostString()+":" + socketChannel.localAddress().getPort());
            Log.debug("handshake failed, close the connetion.");
            socketChannel.close();
            return null;
        }
        if (!isServer) {
            handshakeEvent = new HandshakeEvent(NetworkConstant.HANDSHAKE_CLIENT_TYPE);
            return new NetworkEventResult(true, handshakeEvent);
        }
        return null;
    }

    private NetworkService getNetworkService() {
        if (networkService == null) {
            networkService = NulsContext.getServiceBean(NetworkService.class);
        }
        return networkService;
    }
}
