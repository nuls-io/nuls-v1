package io.nuls.network.service.impl;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.db.dao.BlockDao;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.module.NetworkModule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PeersManager {

    private NetworkModule networkModule;

    private NetworkParam network;

    private PeerDiscovery discovery;

    private static Map<String, PeerGroup> peerGroups = new ConcurrentHashMap<>();

    private static Map<String, Peer> peers = new ConcurrentHashMap<>();


    private BlockDao blockDao;

    public PeersManager(NetworkModule module, NetworkParam network) {
        this.networkModule = module;
        // the default peerGroups
        PeerGroup inPeers = new PeerGroup("inPeers");
        PeerGroup outPeers = new PeerGroup("outPeers");
        PeerGroup consensusPeers = new PeerGroup("consensusPeers");

        peerGroups.put(inPeers.getName(), inPeers);
        peerGroups.put(outPeers.getName(), outPeers);
        peerGroups.put(consensusPeers.getName(), consensusPeers);

        this.discovery = new PeerDiscovery(this);
    }


    public void addPeer(Peer peer) {
        if (!peers.containsKey(peer.getHash())) {
            peers.put(peer.getHash(), peer);
        }
    }

    public void removePeer(String peerHash) {
        if (peers.containsKey(peerHash)) {
            for (PeerGroup group : peerGroups.values()) {
                for (Peer peer : group.getPeers()) {
                    if (peer.getHash().equals(peerHash)) {
                        group.removePeer(peer);
                        break;
                    }
                }
            }

            Peer peer = peers.get(peerHash);
            peer.destroy();
        }
    }


    public void addPeerGroup(String groupName, PeerGroup peerGroup) throws NulsException {
        if (peerGroups.containsKey(groupName)) {
            throw new NulsException(ErrorCode.P2P_GROUP_ALREADY_EXISTS);
        }
        peerGroups.put(groupName, peerGroup);
    }

    public void destroyPeerGroup(String groupName) {

        if (!peerGroups.containsKey(groupName)) {
            return;
        }

        PeerGroup group = peerGroups.get(groupName);
        for (Peer p : group.getPeers()) {
            p.destroy();
            group.removePeer(p);
        }

        peerGroups.remove(groupName);
    }

    public PeerGroup getPeerGroup(String groupName) {
        return peerGroups.get(groupName);
    }

    /**
     * start p2p discovery thread
     * start a peers server
     * query config find original peers
     * query database find cached peers
     * find peers from connetcted peers
     */
    public void start() {


        /** start  heart beat thread
         *
         *
         **/


    }


    public String info() {
        return "";
    }


    private BlockDao getBlockDao() {
        if (blockDao == null) {
            blockDao = NulsContext.getInstance().getService(BlockDao.class);
        }
        return blockDao;
    }
}
