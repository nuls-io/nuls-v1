package io.nuls.network.service;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.network.entity.BroadcastResult;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface Broadcaster{

//    BroadcastResult broadcast(NulsMessage message);
//
//    BroadcastResult broadcastToGroup(NulsMessage message, String groupName);

    BroadcastResult broadcast(BaseNulsEvent event);

    BroadcastResult broadcast(byte[] data);

    BroadcastResult broadcastSync(BaseNulsEvent event);

    BroadcastResult broadcastSync(byte[] data);

    BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName);

    BroadcastResult broadcastToGroup(byte[] data, String groupName);
}
