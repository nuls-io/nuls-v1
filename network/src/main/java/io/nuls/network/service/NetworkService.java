package io.nuls.network.service;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.network.entity.BroadcastResult;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface NetworkService {

    void start();

    void shutdown();

    long currentTimeMillis();

    long currentTimeSeconds();
//
//    BroadcastResult broadcast(NulsMessage message);
//
//    BroadcastResult broadcastToGroup(NulsMessage message, String groupName);

    BroadcastResult broadcast(BaseNulsEvent event);

    BroadcastResult broadcastToGroup(byte[] data, String groupName);

}
