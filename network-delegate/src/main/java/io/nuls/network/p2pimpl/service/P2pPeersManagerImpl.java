package io.nuls.network.p2pimpl.service;

import io.nuls.exception.NulsException;
import io.nuls.exception.NulsRuntimeException;
import io.nuls.global.NulsContext;
import io.nuls.network.intf.IPeersManager;
import io.nuls.network.intf.Peer;
import io.nuls.network.intf.PeerGroup;
import io.nuls.util.constant.ErrorCode;

import java.util.HashMap;
import java.util.Map;

public class P2pPeersManagerImpl implements IPeersManager {
    private static Map<String,PeerGroup> peerGroups = new HashMap<String,PeerGroup>();

    private P2pPeersManagerImpl() {
    }

    private static final P2pPeersManagerImpl service = new P2pPeersManagerImpl();

    public static P2pPeersManagerImpl getInstance() {
        return service;
    }

    @Override
    public void addPeerGroup(String groupName,PeerGroup peerGroup) throws NulsException{
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
