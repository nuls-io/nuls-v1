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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by win10 on 2017/11/10.
 */
public class PeerDiscovery implements Runnable {

    private NetworkParam network;

    private PeersManager peersManager;

    private PeerDao peerDao;

    private AtomicBoolean running = new AtomicBoolean(false);

    private CopyOnWriteArrayList<Peer> seedPeers;

    public PeerDiscovery(PeersManager peersManager) {
        this.peersManager = peersManager;
        this.running.set(true);
    }

    public List<Peer> getPeersFromDataBase() {
        return null;
    }


    public CopyOnWriteArrayList<Peer> getSeedPeers() {
        if (seedPeers == null) {
            seedPeers = new CopyOnWriteArrayList<>();
            for (InetSocketAddress socketAddress : network.getSeedPeers()) {
                seedPeers.add(new Peer(2, socketAddress));
            }
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
        while (running.get()) {
            PeerGroup outPeers = peersManager.getPeerGroup(NetworkConstant.Network_Peer_Out_Group);
            for(Peer peer : outPeers.getPeers()) {
                if(peer.getStatus() == Peer.CLOSE) {
                    peersManager.removePeer(peer.getHash());
                }
            }
            if(outPeers.size() < network.maxOutCount()) {
                // find other peer and try to connect
            }

        }
    }

}
