package io.nuls.network.peer;

import io.nuls.core.exception.NulsException;
import io.nuls.network.entity.PeerGroup;

public interface PeersManager {
    void start();
    void shutdown();
    void restart();
    String info();
    void addPeerGroup(String groupName, PeerGroup peerGroup)throws NulsException;
    void destroyPeerGroup(String groupName);
}
