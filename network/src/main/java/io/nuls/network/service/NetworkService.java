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

    void addNodeToGroup(String area,String groupName,Node node);

    void removeNodeFromGroup(String groupName, String nodeId);

    void removeNodeFromGroup(String area,String groupName,Node node);

    void addNodeGroup(NodeGroup nodeGroup);

    void addNodeGroup(String area,NodeGroup nodeGroup);

    BroadcastResult sendToAllNode(BaseEvent event);

    BroadcastResult sendToAllNode(String area, BaseEvent event);

    BroadcastResult sendToAllNode(BaseEvent event, String excludeNodeId);

    BroadcastResult sendToAllNode(String area, BaseEvent event, String excludeNodeId);

    BroadcastResult sendToAllNode(byte[] data);

    BroadcastResult sendToAllNode(String area, byte[] data);

    BroadcastResult sendToAllNode(byte[] data, String excludeNodeId);

    BroadcastResult sendToAllNode(String area, byte[] data, String excludeNodeId);

    BroadcastResult sendToNode(BaseEvent event, String nodeId);

    BroadcastResult sendToNode(String area, BaseEvent event, String nodeId);

    BroadcastResult sendToNode(byte[] data, String nodeId);

    BroadcastResult sendToNode(String area, byte[] data, String nodeId);

    BroadcastResult sendToGroup(BaseEvent event, String groupName);

    BroadcastResult sendToGroup(String area,BaseEvent event,String groupName);

    BroadcastResult sendToGroup(BaseEvent event, String groupName, String excludeNodeId);

    BroadcastResult sendToGroup(String area, BaseEvent event, String groupName, String excludeNodeId);

    BroadcastResult sendToGroup(byte[] data, String groupName);

    BroadcastResult sendToGroup(String area, byte[] data, String groupName);

    BroadcastResult sendToGroup(byte[] data, String groupName, String excludeNodeId);

    BroadcastResult sendToGroup(String area, byte[] data, String groupName, String excludeNodeId);

}
