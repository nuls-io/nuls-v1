package io.nuls.network.message.impl;

import io.nuls.network.entity.Peer;
import io.nuls.network.message.NetworkMessage;
import io.nuls.network.message.NetworkMessageResult;
import io.nuls.network.message.entity.VersionMessage;
import io.nuls.network.message.messageHandler.NetWorkMessageHandler;

/**
 * Created by vivi on 2017/11/21.
 */
public class VersionMessageHandler implements NetWorkMessageHandler {

    private static final VersionMessageHandler handler = new VersionMessageHandler();

    private VersionMessageHandler() {

    }

    public static VersionMessageHandler getInstance() {
        return handler;
    }


    @Override
    public NetworkMessageResult process(NetworkMessage message, Peer peer) {
        VersionMessage versionMessage = (VersionMessage) message;
        peer.setVersionMessage(versionMessage);

        return null;
    }
}
