package io.nuls.network.service.impl;

import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.PeerDao;
import io.nuls.db.entity.PeerPo;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.PeerTransfer;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.entity.GetPeerData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class PeerDiscoverHandler implements Runnable {

    private AbstractNetworkParam network;

    private PeersManager peersManager;

    private PeerDao peerDao;

    private boolean running;


    public PeerDiscoverHandler(PeersManager peersManager, AbstractNetworkParam network, PeerDao peerDao) {
        this.peersManager = peersManager;
        this.network = network;
        this.running = true;
        this.peerDao = peerDao;
    }

    // get peers from local database
    public List<Peer> getLocalPeers(int size) {
        Set<String> keys = peersManager.getPeers().keySet();
        List<PeerPo> peerPos = peerDao.getRandomPeerPoList(size, keys);

        List<Peer> peers = new ArrayList<>();
        if (peerPos == null || peerPos.isEmpty()) {
            return peers;
        }
        for (PeerPo po : peerPos) {
            Peer peer = new Peer(network);
            PeerTransfer.transferToPeer(peer, po);
            peers.add(peer);
        }

        return peers;
    }


    public List<Peer> getSeedPeers() {
        List<Peer> seedPeers = new ArrayList<>();
        for (InetSocketAddress socketAddress : network.getSeedPeers()) {
            // remove myself
            if (network.getLocalIps().contains(socketAddress.getAddress().getHostAddress())) {
                continue;
            }
            seedPeers.add(new Peer(network, Peer.OUT, socketAddress));
        }
        return seedPeers;
    }

    /**
     * check the peers when closed try to connect other one
     */
    @Override
    public void run() {
        while (running) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            for (Peer peer : peersManager.getPeers().values()) {
                if (peer.getStatus() == Peer.CLOSE) {
                    peersManager.removePeer(peer.getHash());
                }
            }

            PeerGroup outPeers = peersManager.getPeerGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP);
            if (outPeers.size() == 0) {
                //  The seedPeers should be connected immediately
                List<Peer> peers = getSeedPeers();

                for (Peer newPeer : peers) {
                    if (outPeers.getPeers().contains(newPeer)) {
                        continue;
                    }
                    peersManager.addPeerToGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP, newPeer);
                }
            } else if (outPeers.size() < network.maxOutCount()) {
                List<Peer> peers = getLocalPeers(network.maxOutCount() - outPeers.size());
                if (peers.isEmpty()) {
//                    // find other peer from connected peers
                    findOtherPeer(network.maxOutCount() - outPeers.size());
                } else {
                    for (Peer newPeer : peers) {
                        if (outPeers.getPeers().contains(newPeer)) {
                            continue;
                        }
                        peersManager.addPeerToGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP, newPeer);
                    }
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Inquire more of the other peers to the connected peers
     *
     * @param size
     */
    private void findOtherPeer(int size) {
        PeerGroup group = peersManager.getPeerGroup(NetworkConstant.NETWORK_PEER_IN_GROUP);
        if (group.getPeers().size() > 0) {
            Peer peer = group.getPeers().get(0);
            if (peer.isHandShake()) {
                try {
                    GetPeerData data = new GetPeerData(size);
                    peer.sendNetworkData(data);
                } catch (Exception e) {
                    Log.warn("send getPeerData error", e);
                    peer.destroy();
                }
            }
        }

        group = peersManager.getPeerGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP);
        if (group.getPeers().size() > 0) {
            Peer peer = group.getPeers().get(0);
            if (peer.isHandShake()) {
                try {
                    GetPeerData data = new GetPeerData(size);
                    peer.sendNetworkData(data);
                } catch (Exception e) {
                    Log.warn("send getPeerData error", e);
                    peer.destroy();
                }
            }
        }
    }
}
