package io.nuls.network.service;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface Broadcaster {

    BroadcastResult broadcast(BaseNulsEvent event);

    BroadcastResult broadcast(BaseNulsEvent event, String exculdePeerId);

    BroadcastResult broadcast(byte[] data);

    BroadcastResult broadcast(byte[] data, String exculdePeerId);

    BroadcastResult broadcastSync(BaseNulsEvent event);

    BroadcastResult broadcastSync(BaseNulsEvent event, String exculdePeerId);

    BroadcastResult broadcastSync(byte[] data);

    BroadcastResult broadcastSync(byte[] data, String exculdePeerId);

    BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName);

    BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName, String exculdePeerId);

    BroadcastResult broadcastToGroup(byte[] data, String groupName);

    BroadcastResult broadcastToGroup(byte[] data, String groupName, String exculdePeerId);
}
