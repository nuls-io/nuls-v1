package io.nuls.network.service;

import io.nuls.core.mesasge.NulsMessage;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerGroup;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface Broadcaster{

    BroadcastResult broadcast(NulsMessage message);

    BroadcastResult broadcastToGroup(NulsMessage message, String groupName);



}
