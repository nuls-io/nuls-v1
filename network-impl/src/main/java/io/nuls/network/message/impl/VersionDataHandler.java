package io.nuls.network.message.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.network.entity.Peer;
import io.nuls.network.exception.NetworkMessageException;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataResult;
import io.nuls.network.message.entity.VersionData;
import io.nuls.network.message.messageHandler.NetWorkDataHandler;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class VersionDataHandler implements NetWorkDataHandler {

    private static final VersionDataHandler INSTANCE = new VersionDataHandler();

    private VersionDataHandler() {

    }

    public static VersionDataHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkDataResult process(BaseNetworkData message, Peer peer) {
        VersionData versionMessage = (VersionData) message;
        if (versionMessage.getBestBlockHeight() < 0) {
            throw new NetworkMessageException(ErrorCode.NET_MESSAGE_ERROR);
        }
        peer.setVersionMessage(versionMessage);
        peer.setStatus(Peer.HANDSHAKE);
        return null;
    }
}
