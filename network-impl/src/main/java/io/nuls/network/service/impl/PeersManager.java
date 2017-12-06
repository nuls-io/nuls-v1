package io.nuls.network.service.impl;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.module.AbstractNetworkModule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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

    private ReentrantLock lock;

    public PeersManager(AbstractNetworkModule module, AbstractNetworkParam network, PeerDao peerDao) {
        this.networkModule = module;
        this.peerDao = peerDao;
        lock = new ReentrantLock();
        // the default peerGroups
        PeerGroup inPeers = new PeerGroup(NetworkConstant.NETWORK_PEER_IN_GROUP);
        PeerGroup outPeers = new PeerGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP);
        PeerGroup consensusPeers = new PeerGroup(NetworkConstant.NETWORK_PEER_CONSENSUS_GROUP);

        peerGroups.put(inPeers.getName(), inPeers);
        peerGroups.put(outPeers.getName(), outPeers);
        peerGroups.put(consensusPeers.getName(), consensusPeers);

        this.discovery = new PeerDiscoverHandler(this, network, peerDao);
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

//        List<Peer> peers = discovery.getLocalPeers(10);
        List<Peer> peers = discovery.getSeedPeers();

        if (peers == null || peers.size() == 0) {
            peers = discovery.getSeedPeers();
        }

        for (Peer peer : peers) {
            peer.setType(Peer.OUT);
            addPeerToGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP, peer);
        }
        System.out.println("-----------peerManager start");

        //start  heart beat thread
        ThreadManager.createSingleThreadAndRun(AbstractNetworkModule.networkModuleId, "peerDiscovery", this.discovery);
    }


    public void addPeer(Peer peer) {
        lock.lock();
        try {
            if (!peers.containsKey(peer.getHash().toString())) {
                peers.put(peer.getHash(), peer);
                if (!peer.isHandShake() && peer.getType() == Peer.OUT) {
                    connectionManager.openConnection(peer);
                }
            }
        } finally {
            lock.unlock();
        }
    }


    public void addPeerToGroup(String groupName, Peer peer) {
        lock.lock();
        try {
            if (!peerGroups.containsKey(groupName)) {
                throw new NulsRuntimeException(ErrorCode.PEER_GROUP_NOT_FOUND);
            }
            if (groupName.equals(NetworkConstant.NETWORK_PEER_OUT_GROUP) &&
                    peerGroups.get(groupName).size() >= network.maxOutCount()) {
                return;
            }

            if (groupName.equals(NetworkConstant.NETWORK_PEER_IN_GROUP) &&
                    peerGroups.get(groupName).size() >= network.maxInCount()) {
                return;
            }

            addPeer(peer);
            peerGroups.get(groupName).addPeer(peer);
        } finally {
            lock.unlock();
        }
    }


    public void removePeer(String peerHash) {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    public boolean hasPeerGroup(String groupName) {
        return peerGroups.containsKey(groupName);
    }

    public void addPeerGroup(String groupName, PeerGroup peerGroup) throws NulsException {
        if (peerGroups.containsKey(groupName)) {
            throw new NulsException(ErrorCode.PRER_GROUP_ALREADY_EXISTS);
        }
        peerGroups.put(groupName, peerGroup);
    }

    public void destroyPeerGroup(String groupName) {
        lock.lock();
        try {
            if (!peerGroups.containsKey(groupName)) {
                return;
            }

            PeerGroup group = peerGroups.get(groupName);
            for (Peer p : group.getPeers()) {
                p.destroy();
                group.removePeer(p);
            }
            peerGroups.remove(groupName);
        } finally {
            lock.unlock();
        }
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


    public List<Peer> getAvailablePeersByGroup(String groupName) {
        List<Peer> availablePeers = new ArrayList<>();
        if (hasPeerGroup(groupName)) {
            for (Peer peer : getPeerGroup(groupName).getPeers()) {
                if (peer.getStatus() == Peer.HANDSHAKE) {
                    availablePeers.add(peer);
                }
            }
        }
        return availablePeers;
    }


    public List<Peer> getAvailablePeers() {
        List<Peer> availablePeers = new ArrayList<>();
        Collection<Peer> collection = peers.values();
        for (Peer peer : collection) {
            if (peer.getStatus() == Peer.HANDSHAKE) {
                availablePeers.add(peer);
            }
        }
        return availablePeers;
    }

    public List<Peer> getAvailablePeers(int size, Peer excludePeer) {
        List<Peer> availablePeers = getAvailablePeers();
        Collections.shuffle(availablePeers);

        for (Peer peer : availablePeers) {
            if (peer.getStatus() == Peer.HANDSHAKE && peer.getIp().equals(excludePeer)) {
                availablePeers.remove(peer);
                break;
            }
        }
        if (availablePeers.size() <= size) {
            return availablePeers;
        }
        return availablePeers.subList(0, size);
    }

    public int getBroadcasterMinConnectionCount() {
        int count = 0;
        Collection<Peer> collection = peers.values();
        for (Peer peer : collection) {
            if (peer.getStatus() == Peer.HANDSHAKE) {
                count++;
            }
        }
        if (count <= 1) {
            return count;
        } else {
            return Math.max(1, (int) (count * 0.8));
        }
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
