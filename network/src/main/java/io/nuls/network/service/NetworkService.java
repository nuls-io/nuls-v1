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

    void addPeer(Peer peer);

    void addPeerToGroup(String groupName, Peer peer);

    void addPeerGroup(PeerGroup peerGroup);

    BroadcastResult broadcast(BaseNulsEvent event);

    BroadcastResult broadcast(BaseNulsEvent event, String excludePeerId);

    BroadcastResult broadcast(byte[] data);

    BroadcastResult broadcast(byte[] data, String excludePeerId);

    BroadcastResult broadcastSync(BaseNulsEvent event);

    BroadcastResult broadcastSync(BaseNulsEvent event, String excludePeerId);

    BroadcastResult broadcastSync(byte[] data);

    BroadcastResult broadcastSync(byte[] data, String excludePeerId);

    BroadcastResult broadcastToPeer(BaseNulsEvent event, String peerId);

    BroadcastResult broadcastToPeer(byte[] data, String peerId);

    BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName);

    BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName, String excludePeerId);

    BroadcastResult broadcastToGroup(byte[] data, String groupName);

    BroadcastResult broadcastToGroup(byte[] data, String groupName, String excludePeerId);

    BroadcastResult broadcastToGroupSync(BaseNulsEvent event, String groupName);

    BroadcastResult broadcastToGroupSync(BaseNulsEvent event, String groupName, String excludePeerId);

    BroadcastResult broadcastToGroupSync(byte[] data, String groupName);

    BroadcastResult broadcastToGroupSync(byte[] data, String groupName, String excludePeerId);
}
