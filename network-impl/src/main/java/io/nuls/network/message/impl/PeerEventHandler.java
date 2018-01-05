package io.nuls.network.message.impl;

import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.PeerEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.impl.PeersManager;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class PeerEventHandler implements NetWorkEventHandler {

    private static final PeerEventHandler INSTANCE = new PeerEventHandler();

    private PeersManager peersManager;

    private PeerEventHandler() {

    }

    public static PeerEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseNetworkEvent networkEvent, Peer peer) {
        PeerEvent event = (PeerEvent) networkEvent;
        for (Peer newPeer : event.getEventBody().getPeers()) {
            newPeer.setType(Peer.OUT);
            newPeer.setMessageHandlerFactory(peer.getMessageHandlerFactory());
            peersManager.addPeerToGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP, newPeer);
        }
        return null;
    }

    public void setPeersManager(PeersManager peersManager) {
        this.peersManager = peersManager;
    }
}
