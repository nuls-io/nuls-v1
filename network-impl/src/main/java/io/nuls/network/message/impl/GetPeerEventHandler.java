package io.nuls.network.message.impl;

import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.network.entity.Peer;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.GetPeerEvent;
import io.nuls.network.message.entity.PeerEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.impl.PeersManager;

import java.util.List;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class GetPeerEventHandler implements NetWorkEventHandler {

    private static final GetPeerEventHandler INSTANCE = new GetPeerEventHandler();

    private PeersManager peersManager;

    private GetPeerEventHandler() {

    }

    public static GetPeerEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseNetworkEvent event, Peer peer) {
        GetPeerEvent getPeerEvent = (GetPeerEvent) event;

        List<Peer> list = peersManager.getAvailablePeers(getPeerEvent.getEventBody().getVal(), peer);
        PeerEvent replyData = new PeerEvent();
        replyData.setPeers(list);
        return new NetworkEventResult(true, replyData);
    }

    public void setPeersManager(PeersManager peersManager) {
        this.peersManager = peersManager;
    }
}
