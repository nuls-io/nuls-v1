package io.nuls.network.service.impl;


import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.network.IPUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.entity.PeerTransfer;
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

    private AbstractNetworkParam network;

    private PeerDiscoverHandler discovery;

    private ConnectionManager connectionManager;

    private static Map<String, PeerGroup> peerGroups = new ConcurrentHashMap<>();

    private static Map<String, Peer> peers = new ConcurrentHashMap<>();

    private PeerDao peerDao;

    private ReentrantLock lock;

    private List<Peer> seedPeers;

    public PeersManager(AbstractNetworkModule module, AbstractNetworkParam network, PeerDao peerDao) {
        this.network = network;
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
     * find other peers from connetcted peers
     */
    public void start() {
        List<Peer> peers = discovery.getLocalPeers(10);
        if (peers.isEmpty()) {
            peers = getSeedPeers();
        }

        for (Peer peer : peers) {
            peer.setType(Peer.OUT);
            addPeerToGroup(NetworkConstant.NETWORK_PEER_OUT_GROUP, peer);
        }

        boolean isConsensus = NulsContext.MODULES_CONFIG.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_DELEGATE_PEER, false);
        if (isConsensus) {
            network.maxOutCount(network.maxOutCount() * 5);
            network.maxInCount(network.maxInCount() * 5);
        }

        System.out.println("-----------peerManager start");
        //start  heart beat thread
        ThreadManager.createSingleThreadAndRun(NulsConstant.MODULE_ID_NETWORK, "peerDiscovery", this.discovery);
    }

    public List<Peer> getSeedPeers() {
        if (seedPeers == null) {
            seedPeers = discovery.getSeedPeers();
        }
        return seedPeers;
    }

    /**
     * when peerId is null, check myself
     *
     * @param peerId
     * @return
     */
    public boolean isSeedPeers(String peerId) {
        if (StringUtils.isBlank(peerId)) {
            Set<String> ips = IPUtil.getIps();
            for (String self : ips) {
                for (Peer peer : getSeedPeers()) {
                    if (peer.getHash().equals(self)) {
                        return true;
                    }
                }
            }
        } else {
            for (Peer peer : getSeedPeers()) {
                if (peer.getHash().equals(peerId)) {
                    return true;
                }
            }
        }
        return false;
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
                if (!isSeedPeers(peerHash)) {
                    Peer peer = peers.get(peerHash);
                    peer.setFailCount(peer.getFailCount() + 1);
                    peerDao.saveChange(PeerTransfer.transferToPeerPo(peer));
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

    public void addPeerGroup(PeerGroup peerGroup) {
        if (peerGroups.containsKey(peerGroup.getName())) {
            throw new NulsRuntimeException(ErrorCode.PRER_GROUP_ALREADY_EXISTS);
        }
        peerGroups.put(peerGroup.getName(), peerGroup);
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
        peer.destroy();
        peerDao.deleteByKey(peer.getHash());
    }

    public Peer getPeer(String peerId) {
        return peers.get(peerId);
    }

    public Map<String, Peer> getPeers() {
        return peers;
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


    public List<Peer> getAvailablePeers(String excludePeerId) {
        List<Peer> availablePeers = new ArrayList<>();
        Collection<Peer> collection = peers.values();
        for (Peer peer : collection) {
            if (peer.getStatus() == Peer.HANDSHAKE && !peer.getIp().equals(excludePeerId)) {
                availablePeers.add(peer);
            }
        }
        return availablePeers;
    }

    public List<Peer> getAvailablePeers(int size, Peer excludePeer) {
        List<Peer> availablePeers = getAvailablePeers(excludePeer.getHash());
        if (availablePeers.size() <= size) {
            return availablePeers;
        }
        Collections.shuffle(availablePeers);
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

    public List<Peer> getGroupAvailablePeers(String groupName, String excludePeerId) {
        if (!peerGroups.containsKey(groupName)) {
            throw new NulsRuntimeException(ErrorCode.PEER_GROUP_NOT_FOUND);
        }
        List<Peer> availablePeers = new ArrayList<>();
        PeerGroup group = peerGroups.get(groupName);
        for (Peer peer : group.getPeers()) {
            if (peer.getStatus() == Peer.HANDSHAKE && !peer.getIp().equals(excludePeerId)) {
                availablePeers.add(peer);
            }
        }
        return availablePeers;
    }




    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
}
