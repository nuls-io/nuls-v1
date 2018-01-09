package io.nuls.network.service;

import io.nuls.core.event.BaseEvent;
import io.nuls.network.entity.BroadcastResult;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface Broadcaster {

    BroadcastResult broadcast(BaseEvent event);

    BroadcastResult broadcast(BaseEvent event, String excludeNodeId);

    BroadcastResult broadcast(byte[] data);

    BroadcastResult broadcast(byte[] data, String excludeNodeId);

//    BroadcastResult broadcastSync(BaseNulsEvent event);
//
//    BroadcastResult broadcastSync(BaseNulsEvent event, String excludeNodeId);
//
//    BroadcastResult broadcastSync(byte[] data);
//
//    BroadcastResult broadcastSync(byte[] data, String excludeNodeId);

    BroadcastResult broadcastToNode(BaseEvent event, String nodeId);

    BroadcastResult broadcastToNode(byte[] data, String nodeId);

    BroadcastResult broadcastToGroup(BaseEvent event, String groupName);

    BroadcastResult broadcastToGroup(BaseEvent event, String groupName, String excludeNodeId);

    BroadcastResult broadcastToGroup(byte[] data, String groupName);

    BroadcastResult broadcastToGroup(byte[] data, String groupName, String excludeNodeId);

//    BroadcastResult broadcastToGroupSync(BaseNulsEvent event, String groupName);
//
//    BroadcastResult broadcastToGroupSync(BaseNulsEvent event, String groupName, String excludeNodeId);
//
//    BroadcastResult broadcastToGroupSync(byte[] data, String groupName);
//
//    BroadcastResult broadcastToGroupSync(byte[] data, String groupName, String excludeNodeId);

}
