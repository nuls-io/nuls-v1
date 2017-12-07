package io.nuls.network.message.impl;

import io.nuls.network.entity.Peer;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataResult;
import io.nuls.network.message.entity.GetVersionData;
import io.nuls.network.message.entity.VersionData;
import io.nuls.network.message.messageHandler.NetWorkDataHandler;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionDataHandler implements NetWorkDataHandler {

    private static final GetVersionDataHandler INSTANCE = new GetVersionDataHandler();

    private GetVersionDataHandler() {

    }

    public static GetVersionDataHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkDataResult process(BaseNetworkData message, Peer peer) {
        GetVersionData data = (GetVersionData) message;
        VersionData replyMessage = new VersionData(1111, "ABCDEFGHTK", peer.getIp());
        peer.setPort(data.getExternalPort());
        return new NetworkDataResult(true, replyMessage);
    }
}
