package io.nuls.network.service;

import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.network.entity.BroadcastResult;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface Broadcaster {

    BroadcastResult broadcast(BaseNetworkEvent event);

    BroadcastResult broadcast(BaseNetworkEvent event, String excludeNodeId);

    BroadcastResult broadcast(byte[] data);

    BroadcastResult broadcast(byte[] data, String excludeNodeId);

//    BroadcastResult broadcastSync(BaseNulsEvent event);
//
//    BroadcastResult broadcastSync(BaseNulsEvent event, String excludeNodeId);
//
//    BroadcastResult broadcastSync(byte[] data);
//
//    BroadcastResult broadcastSync(byte[] data, String excludeNodeId);

    BroadcastResult broadcastToNode(BaseNetworkEvent event, String nodeId);

    BroadcastResult broadcastToNode(byte[] data, String nodeId);

    BroadcastResult broadcastToGroup(BaseNetworkEvent event, String groupName);

    BroadcastResult broadcastToGroup(BaseNetworkEvent event, String groupName, String excludeNodeId);

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
