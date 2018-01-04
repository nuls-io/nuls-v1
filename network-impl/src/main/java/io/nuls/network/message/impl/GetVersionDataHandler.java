package io.nuls.network.message.impl;

import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.network.entity.Peer;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.GetVersionEvent;
import io.nuls.network.message.entity.VersionData;
import io.nuls.network.message.handler.NetWorkEventHandler;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionDataHandler implements NetWorkEventHandler {
    @Override
    public NetworkEventResult process(BaseNetworkEvent message, Peer peer) {
        return null;
    }

//    private static final GetVersionDataHandler INSTANCE = new GetVersionDataHandler();
//
//    private GetVersionDataHandler() {
//
//    }
//
//    public static GetVersionDataHandler getInstance() {
//        return INSTANCE;
//    }
//
//    @Override
//    public NetworkEventResult process(BaseNetworkData message, Peer peer) {
//        GetVersionEvent data = (GetVersionEvent) message;
//        VersionData replyMessage = new VersionData(1111, "ABCDEFGHTK");
//        peer.setPort(data.getExternalPort());
//        return new NetworkEventResult(true, replyMessage);
//    }
}
