package io.nuls.network.service;

import io.nuls.core.event.BaseNetworkEvent;
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

    boolean isSeedPeer(String peerId);

    boolean isSeedPeer();

    void addPeer(Peer peer);

    void removePeer(String peerId);

    void addPeerToGroup(String groupName, Peer peer);

    void addPeerGroup(PeerGroup peerGroup);

    BroadcastResult broadcast(BaseNetworkEvent event);

    BroadcastResult broadcast(BaseNetworkEvent event, String excludePeerId);

    BroadcastResult broadcast(byte[] data);

    BroadcastResult broadcast(byte[] data, String excludePeerId);

    BroadcastResult broadcastSync(BaseNetworkEvent event);

    BroadcastResult broadcastSync(BaseNetworkEvent event, String excludePeerId);

    BroadcastResult broadcastSync(byte[] data);

    BroadcastResult broadcastSync(byte[] data, String excludePeerId);

    BroadcastResult broadcastToPeer(BaseNetworkEvent event, String peerId);

    BroadcastResult broadcastToPeer(byte[] data, String peerId);

    BroadcastResult broadcastToGroup(BaseNetworkEvent event, String groupName);

    BroadcastResult broadcastToGroup(BaseNetworkEvent event, String groupName, String excludePeerId);

    BroadcastResult broadcastToGroup(byte[] data, String groupName);

    BroadcastResult broadcastToGroup(byte[] data, String groupName, String excludePeerId);

    BroadcastResult broadcastToGroupSync(BaseNetworkEvent event, String groupName);

    BroadcastResult broadcastToGroupSync(BaseNetworkEvent event, String groupName, String excludePeerId);

    BroadcastResult broadcastToGroupSync(byte[] data, String groupName);

    BroadcastResult broadcastToGroupSync(byte[] data, String groupName, String excludePeerId);

}
