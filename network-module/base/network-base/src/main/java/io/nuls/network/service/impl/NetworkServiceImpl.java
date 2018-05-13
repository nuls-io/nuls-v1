package io.nuls.network.service.impl;

import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.service.NetworkService;

import java.util.Collection;
import java.util.Map;

@Service
public class NetworkServiceImpl implements NetworkService {

    private NodeManager nodeManager = NodeManager.getInstance();

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
        return null;
    }

    @Override
    public Collection<Node> getAvailableNodes() {
        return nodeManager.getAvailableNodes();
    }

    @Override
    public BroadcastResult sendToAllNode(BaseNulsData event, boolean asyn) {
        return null;
    }

    @Override
    public BroadcastResult sendToAllNode(BaseNulsData event, Node excludeNode, boolean asyn) {
        return null;
    }

    @Override
    public BroadcastResult sendToNode(BaseNulsData event, Node node, boolean asyn) {
        return null;
    }

    @Override
    public BroadcastResult sendToGroup(BaseNulsData event, String groupName, boolean asyn) {
        return null;
    }

    @Override
    public BroadcastResult sendToGroup(BaseNulsData event, String groupName, String excludeNodeId, boolean asyn) {
        return null;
    }

    @Override
    public boolean reset() {
        return false;
    }

    @Override
    public NetworkParam getNetworkParam() {
        return null;
    }
}
