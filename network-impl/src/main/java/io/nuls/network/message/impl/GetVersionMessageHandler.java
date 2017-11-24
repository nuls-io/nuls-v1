package io.nuls.network.message.impl;

import io.nuls.network.entity.Peer;
import io.nuls.network.message.AbstractNetworkMessage;
import io.nuls.network.message.NetworkMessageResult;
import io.nuls.network.message.entity.VersionMessage;
import io.nuls.network.message.messageHandler.NetWorkMessageHandler;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionMessageHandler implements NetWorkMessageHandler {

    private static final GetVersionMessageHandler INSTANCE = new GetVersionMessageHandler();

    private GetVersionMessageHandler() {

    }

    public static GetVersionMessageHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkMessageResult process(AbstractNetworkMessage message, Peer peer) {
        VersionMessage replyMessage = new VersionMessage(1111,"ABCDEFGHTK");
        return new NetworkMessageResult(true, replyMessage);
    }
}
