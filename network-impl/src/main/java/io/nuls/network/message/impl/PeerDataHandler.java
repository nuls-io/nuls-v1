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
public class PeerDataHandler implements NetWorkEventHandler {
    @Override
    public NetworkEventResult process(BaseNetworkEvent message, Peer peer) {
        return null;
    }

//    private static final PeerDataHandler INSTANCE = new PeerDataHandler();
//
//    private PeersManager peersManager;
//
//    private PeerDataHandler() {
//
//    }
//
//    public static PeerDataHandler getInstance() {
//        return INSTANCE;
//    }
//
//    @Override
//    public NetworkEventResult process(BaseNetworkData message, Peer peer) {
//        PeerEvent peerData = (PeerEvent) message;
//        for (Peer newPeer : peerData.getPeers()) {
//            newPeer.setType(Peer.OUT);
//            newPeer.setMessageHandlerFactory(peer.getMessageHandlerFactory());
//            peersManager.addPeerToGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP, newPeer);
//        }
//        return null;
//    }
//
//    public void setPeersManager(PeersManager peersManager) {
//        this.peersManager = peersManager;
//    }
}
