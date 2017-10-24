package io.nuls.network.entity;

import java.util.List;

public class PeerGroup {
    private String groupName = null;
    private List<Peer> peers = null;

    public void setName(String name){
        this.groupName = name;
    }

    public String getName(){
        return groupName;
    }

    public List<Peer> getPeers(){
        return peers;
    }

    public void addPeer(Peer p){
        this.peers.add(p);
        //TODO, 去重复
    }
    public void addGroup(PeerGroup peerGroup){
        for(Peer p:peerGroup.getPeers()){
            addPeer(p);
        }
    }

    public void removePeer(Peer p){
        //TODO ,delete peer
    }

    public int size(){
        return peers.size();
    }

    public void removeAll(){
        peers.clear();
    }

    public String toString(){
        return "";
    }
}
