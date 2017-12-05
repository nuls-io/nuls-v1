package io.nuls.network.entity;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class PeerGroup {
    private String groupName;
    private CopyOnWriteArrayList<Peer> peers;

    public PeerGroup(String groupName) {
        this.groupName = groupName;
        peers = new CopyOnWriteArrayList<>();
    }

    public CopyOnWriteArrayList<Peer> getPeers() {
        return peers;
    }

    public void addPeer(Peer p) {
        for (Peer peer : peers) {
            if (peer.getHash().toString().equals(p.getHash().toString())) {
                return;
            }
        }
        this.peers.add(p);
    }

    public void removePeer(Peer p) {
        this.peers.remove(p);
    }

    public int size() {
        return peers.size();
    }

    public void removeAll() {
        peers.clear();
    }

    @Override
    public String toString() {
        return "";
    }


    public void setName(String name) {
        this.groupName = name;
    }

    public String getName() {
        return groupName;
    }
}
