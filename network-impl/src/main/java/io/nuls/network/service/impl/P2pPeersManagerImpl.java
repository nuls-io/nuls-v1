package io.nuls.network.service.impl;



import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;
import io.nuls.network.peer.PeersManager;

import java.util.HashMap;
import java.util.Map;

public class P2pPeersManagerImpl implements PeersManager {
    private static Map<String,PeerGroup> peerGroups = new HashMap<String,PeerGroup>();

    private P2pPeersManagerImpl() {
    }

    private static final P2pPeersManagerImpl service = new P2pPeersManagerImpl();

    public static P2pPeersManagerImpl getInstance() {
        return service;
    }

    @Override
    public void addPeerGroup(String groupName,PeerGroup peerGroup) throws NulsException {
        if(peerGroups.containsKey(groupName)) {
            throw new NulsException(ErrorCode.P2P_GROUP_ALREADY_EXISTS);
        }
        peerGroups.put(groupName,peerGroup);
    }

    @Override
    public void destroyPeerGroup(String groupName) {

        if(!peerGroups.containsKey(groupName)) {
            return;
        }

        PeerGroup group = peerGroups.get(groupName);
        for(Peer p : group.getPeers()){
            p.destroy();
            group.removePeer(p);
        }

        peerGroups.remove(groupName);
    }


    @Override
    public void start() {
        /** start p2p discovery thread
         *    query config find original peers
         *    query database find cached peers
         *    find peers from connetcted peers
         *
         */

        /** start  heart beat thread
         *
         *
         **/


    }

    @Override
    public void shutdown() {

    }

    @Override
    public void restart() {

    }

    @Override
    public String info() {
        return "";
    }
}
