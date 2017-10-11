package io.nuls.network.intf;


import io.nuls.message.NulsMessage;


public abstract class IBroadcaster {

    public BroadcastResult boradcast(NulsMessage msg, PeerGroup peerGroup, boolean isSyn){
        //Log.debug("msg={},peerGroup={},isSyn={}",msg,peerGroup,isSyn);
        BroadcastResult br = doboradcast(msg,peerGroup,true);
        //Log.debug("result={}",br);
        return br;
    }

    public BroadcastResult boradcast(NulsMessage msg, PeerGroup peerGroup){
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
