package io.nuls.network.service.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.param.AbstractNetworkParam;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by win10 on 2017/11/10.
 */
public class PeerDiscoverHandler implements Runnable {

    private AbstractNetworkParam network;

    private PeersManager peersManager;

    private PeerDao peerDao;

    private boolean running;


    public PeerDiscoverHandler(PeersManager peersManager, AbstractNetworkParam network) {
        this.peersManager = peersManager;
        this.network = network;
        this.running = true;
    }

    // get peers from local database
    public List<Peer> getLocalPeers() {
        //todo find from database
        return null;
    }


    public List<Peer> getSeedPeers() {
        List<Peer> seedPeers = new ArrayList<>();
        for (InetSocketAddress socketAddress : network.getSeedPeers()) {
            // remove myself
            if(network.getLocalIps().contains(socketAddress.getAddress().getHostAddress())) {
                continue;
            }
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
            PeerGroup outPeers = peersManager.getPeerGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP);
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
