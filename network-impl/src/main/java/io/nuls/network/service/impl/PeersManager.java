package io.nuls.network.service.impl;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.BaseNulsThread;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.module.AbstractNetworkModule;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class PeersManager {

    private AbstractNetworkModule networkModule;

    private AbstractNetworkParam network;

    private PeerDiscoverHandler discovery;

    private ConnectionManager connectionManager;

    private static Map<String, PeerGroup> peerGroups = new ConcurrentHashMap<>();

    private static Map<String, Peer> peers = new ConcurrentHashMap<>();

    private BlockDao blockDao;

    private PeerDao peerDao;

    public PeersManager(AbstractNetworkModule module, AbstractNetworkParam network, PeerDao peerDao) {
        this.networkModule = module;
        this.peerDao = peerDao;
        // the default peerGroups
        PeerGroup inPeers = new PeerGroup(NetworkConstant.NETWORK_PEER_IN_GROUP);
        PeerGroup outPeers = new PeerGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP);
        PeerGroup consensusPeers = new PeerGroup(NetworkConstant.NETWORK_PEER_CONSENSUS_GROUP);

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
            addPeerToGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP, peer);
            connectionManager.openConnection(peer);
        }

        /** start  heart beat thread
         *
         *
         **/
        new BaseNulsThread(networkModule, "peerDiscovery") {
            @Override
            public void run() {
                PeersManager.this.discovery.run();
            }
        }.start();
    }


    public void addPeer(Peer peer) {
        if (!peers.containsKey(peer.getHash())) {
            peers.put(peer.getHash(), peer);
        }
    }


    public void addPeerToGroup(String groupName, Peer peer) {
        if (!peerGroups.containsKey(groupName)) {
            throw new NulsRuntimeException(ErrorCode.PEER_GROUP_NOT_FOUND);
        }

        addPeer(peer);
        peerGroups.get(groupName).addPeer(peer);
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
            peers.remove(peerHash);
        }
    }


    public void addPeerGroup(String groupName, PeerGroup peerGroup) throws NulsException {
        if (peerGroups.containsKey(groupName)) {
            throw new NulsException(ErrorCode.PRER_GROUP_ALREADY_EXISTS);
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

    /**
     * remove from database
     *
     * @param peer
     */
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

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
