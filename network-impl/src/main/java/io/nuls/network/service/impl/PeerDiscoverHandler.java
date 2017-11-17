package io.nuls.network.service.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.module.impl.NetworkModuleImpl;
import io.nuls.network.param.DevNetworkParam;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by win10 on 2017/11/10.
 */
public class PeerDiscoverHandler implements Runnable {

    private NetworkParam network;

    private PeersManager peersManager;

    private PeerDao peerDao;

    private boolean running;


    public PeerDiscoverHandler(PeersManager peersManager, NetworkParam network) {
        this.peersManager = peersManager;
        this.network = network;
        this.running = true;
    }

    // get peers from local database
    public CopyOnWriteArrayList<Peer> getLocalPeers() {
        return null;
    }


    public List<Peer> getSeedPeers() {
        List<Peer> seedPeers = new ArrayList<>();
        for (InetSocketAddress socketAddress : network.getSeedPeers()) {
            seedPeers.add(new Peer(network, Peer.OUT, socketAddress));
        }
        return seedPeers;
    }

    private PeerDao getPeerDao() {
        if (peerDao == null) {
            peerDao = NulsContext.getInstance().getService(PeerDao.class);
        }
        return peerDao;
    }

    /**
     * check the peers when closed try to connect other one
     */
    @Override
    public void run() {
        while (running) {
            PeerGroup outPeers = peersManager.getPeerGroup(NetworkConstant.Network_Peer_Out_Group);
            for (Peer peer : outPeers.getPeers()) {
                if (peer.getStatus() == Peer.CLOSE) {
                    peersManager.removePeer(peer.getHash());
                }
            }
            if (outPeers.size() < network.maxOutCount()) {
                // find other peer and try to connect
            }

        }
    }

}
