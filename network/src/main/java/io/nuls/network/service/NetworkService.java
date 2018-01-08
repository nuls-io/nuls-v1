package io.nuls.network.service;

import io.nuls.core.event.BaseEvent;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface NetworkService {

    void init();

    void start();

    void shutdown();

    void addNode(Node node);

    void removeNode(String nodeId);

    void addNodeToGroup(String groupName, Node node);

    void removeNodeFromGroup(String groupName, String nodeId);

    void addNodeGroup(NodeGroup nodeGroup);

    BroadcastResult sendToAllNode(BaseEvent event);

    BroadcastResult sendToAllNode(BaseEvent event, String excludeNodeId);

    BroadcastResult sendToAllNode(byte[] data);

    BroadcastResult sendToAllNode(byte[] data, String excludeNodeId);

    BroadcastResult sendToNode(BaseEvent event, String nodeId);

    BroadcastResult sendToNode(byte[] data, String nodeId);

    BroadcastResult sendToGroup(BaseEvent event, String groupName);

    BroadcastResult sendToGroup(BaseEvent event, String groupName, String excludeNodeId);

    BroadcastResult sendToGroup(byte[] data, String groupName);

    BroadcastResult sendToGroup(byte[] data, String groupName, String excludeNodeId);

}
