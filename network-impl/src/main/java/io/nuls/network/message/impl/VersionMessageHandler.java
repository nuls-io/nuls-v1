package io.nuls.network.message.impl;

import io.nuls.network.entity.Peer;
import io.nuls.network.message.AbstractNetworkMessage;
import io.nuls.network.message.AbstractNetworkMessageResult;
import io.nuls.network.message.entity.VersionMessage;
import io.nuls.network.message.messageHandler.NetWorkMessageHandler;

/**
 *
 * @author vivi
 * @date 2017/11/21
 */
public class VersionMessageHandler implements NetWorkMessageHandler {

    private static final VersionMessageHandler INSTANCE = new VersionMessageHandler();

    private VersionMessageHandler() {

    }

    public static VersionMessageHandler getInstance() {
        return INSTANCE;
    }


    @Override
    public AbstractNetworkMessageResult process(AbstractNetworkMessage message, Peer peer) {
        VersionMessage versionMessage = (VersionMessage) message;
        peer.setVersionMessage(versionMessage);

        return null;
    }
}
