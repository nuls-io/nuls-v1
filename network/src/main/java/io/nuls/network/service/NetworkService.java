package io.nuls.network.service;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;

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
}
