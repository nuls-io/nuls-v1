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
       // broadcastHandler.broadcast()
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
        return NetworkParam.getInstance();
    }
}
