package io.nuls.network.service;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface NetworkService {

    void start();

    void shutdown();

    BroadcastResult broadcast(BaseNulsEvent event);

    BroadcastResult broadcast(byte[] data);

    BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName);

    BroadcastResult broadcastToGroup(byte[] data, String groupName);

    void addPeer(Peer peer);

    void addPeerToGroup(String groupName, Peer peer);

    void addPeerGroup(PeerGroup peerGroup);
}
