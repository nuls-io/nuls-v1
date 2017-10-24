package io.nuls.network.intf;

import io.nuls.exception.NulsException;

import java.util.Map;

public interface IPeersManager {
    void start();
    void shutdown();
    void restart();
    String info();
    void addPeerGroup(String groupName,PeerGroup peerGroup)throws NulsException;
    void destroyPeerGroup(String groupName);
}
