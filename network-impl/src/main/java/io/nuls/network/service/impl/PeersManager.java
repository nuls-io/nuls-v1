package io.nuls.network.service.impl;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.module.NetworkModule;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PeersManager {

    private NetworkModule networkModule;

    private NetworkParam network;

    private PeerDiscoverHandler discovery;

    private ConnectionManager connectionManager;

    private static Map<String, PeerGroup> peerGroups = new ConcurrentHashMap<>();

    private static Map<String, Peer> peers = new ConcurrentHashMap<>();

    private BlockDao blockDao;

    private PeerDao peerDao;

    public PeersManager(NetworkModule module, NetworkParam network, PeerDao peerDao) {
        this.networkModule = module;
        this.peerDao = peerDao;
        // the default peerGroups
        PeerGroup inPeers = new PeerGroup(NetworkConstant.Network_Peer_In_Group);
        PeerGroup outPeers = new PeerGroup(NetworkConstant.Network_Peer_Out_Group);
        PeerGroup consensusPeers = new PeerGroup(NetworkConstant.Network_Peer_Consensus_Group);

        peerGroups.put(inPeers.getName(), inPeers);
        peerGroups.put(outPeers.getName(), outPeers);
        peerGroups.put(consensusPeers.getName(), consensusPeers);

        this.discovery = new PeerDiscoverHandler(this, network);
    }

    /**
     * 1. get peers from database
     * start p2p discovery thread
     * start a peers server
     * query config find original peers
     * query database find cached peers
     * find peers from connetcted peers
     */
    public void start() {
        List<Peer> peers = discovery.getLocalPeers();

        if (peers == null) {
            peers = discovery.getSeedPeers();
        }
        for (Peer peer : peers) {
            connectionManager.openConnection(peer);
        }

        /** start  heart beat thread
         *
         *
         **/


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


    public void deletePeer(Peer peer) {
        peerDao.deleteByKey(peer.getHash());
    }

    public PeerGroup getPeerGroup(String groupName) {
        return peerGroups.get(groupName);
    }


    public String info() {
        return "";
    }


    private BlockDao getBlockDao() {
        if (blockDao == null) {
            while (NulsContext.getInstance().getService(BlockDao.class) == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
            blockDao = NulsContext.getInstance().getService(BlockDao.class);
        }
        return blockDao;
    }



    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
}
