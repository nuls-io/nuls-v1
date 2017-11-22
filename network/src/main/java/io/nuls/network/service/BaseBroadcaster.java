package io.nuls.network.service;

import io.nuls.core.mesasge.NulsMessage;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;

public abstract class BaseBroadcaster<T extends NulsMessage> {

    public BroadcastResult boradcast(T msg, PeerGroup peerGroup, boolean isSyn){
        //Log.debug("msg={},peerGroup={},isSyn={}",msg,peerGroup,isSyn);
        BroadcastResult br = doboradcast(msg,peerGroup,true);
        //Log.debug("result={}",br);
        return br;
    }

    public BroadcastResult boradcast(T msg, PeerGroup peerGroup){
        return boradcast(msg, peerGroup,true);
    }

    public BroadcastResult broadcast(NulsMessage msg, PeerGroup peerGroup, Peer excludePeer, boolean isSyn){
        return null;
    }

    public BroadcastResult broadcast(NulsMessage msg, PeerGroup peerGroup, Peer excludePeer){
        return broadcast(msg, peerGroup, excludePeer, true);
    }

    abstract BroadcastResult doboradcast(NulsMessage msg,PeerGroup peerGroup,boolean isSyn);
    abstract BroadcastResult doboradcast(NulsMessage msg,PeerGroup peerGroup,Peer excludePeer,boolean isSyn);

}
