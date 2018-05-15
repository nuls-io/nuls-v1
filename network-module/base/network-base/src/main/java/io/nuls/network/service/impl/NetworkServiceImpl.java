package io.nuls.network.service.impl;

import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.manager.BroadcastHandler;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.Collection;
import java.util.Map;

@Service
public class NetworkServiceImpl implements NetworkService {

    private NodeManager nodeManager = NodeManager.getInstance();

    private BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    @Override
    public void removeNode(String nodeId) {
        nodeManager.removeHandshakeNode(nodeId);
    }

    @Override
    public Node getNode(String nodeId) {
        return nodeManager.getNode(nodeId);
    }

    @Override
    public Map<String, Node> getNodes() {
        return nodeManager.getNodes();
    }

    @Override
    public Collection<Node> getAvailableNodes() {
        return nodeManager.getAvailableNodes();
    }

    @Override
    public BroadcastResult sendToAllNode(BaseNulsData nulsData, boolean asyn) {
        BaseMessage baseMessage = (BaseMessage) nulsData;
        return broadcastHandler.broadcast(baseMessage, asyn);
    }

    @Override
    public BroadcastResult sendToAllNode(BaseNulsData nulsData, Node excludeNode, boolean asyn) {
        BaseMessage baseMessage = (BaseMessage) nulsData;
        return broadcastHandler.broadcast(baseMessage, excludeNode.getId(), asyn);
    }

    @Override
    public BroadcastResult sendToNode(BaseNulsData nulsData, Node node, boolean asyn) {
        BaseMessage baseMessage = (BaseMessage) nulsData;
        return broadcastHandler.broadcastToNode(baseMessage, node, asyn);
    }

    @Override
    public BroadcastResult sendToGroup(BaseNulsData nulsData, String groupName, boolean asyn) {
        BaseMessage baseMessage = (BaseMessage) nulsData;
        return broadcastHandler.broadcastToNodeGroup(baseMessage, groupName, asyn);
    }

    @Override
    public BroadcastResult sendToGroup(BaseNulsData nulsData, String groupName, String excludeNodeId, boolean asyn) {
        BaseMessage baseMessage = (BaseMessage) nulsData;
        return broadcastHandler.broadcastToNodeGroup(baseMessage, groupName, excludeNodeId, asyn);
    }

    @Override
    public void reset() {
        nodeManager.reset();
    }

    @Override
    public NetworkParam getNetworkParam() {
        return NetworkParam.getInstance();
    }
}
