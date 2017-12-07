package io.nuls.network.message.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataResult;
import io.nuls.network.message.entity.GetPeerData;
import io.nuls.network.message.entity.PeerData;
import io.nuls.network.message.messageHandler.NetWorkDataHandler;
import io.nuls.network.service.impl.PeersManager;

import java.util.List;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class PeerDataHandler implements NetWorkDataHandler {

    private static final PeerDataHandler INSTANCE = new PeerDataHandler();

    private PeersManager peersManager;

    private PeerDataHandler() {

    }

    public static PeerDataHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkDataResult process(BaseNetworkData message, Peer peer) {
        PeerData peerData = (PeerData) message;
        for (Peer newPeer : peerData.getPeers()) {
            System.out.println(newPeer);
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
