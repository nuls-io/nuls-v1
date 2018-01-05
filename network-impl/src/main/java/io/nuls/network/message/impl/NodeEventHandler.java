package io.nuls.network.message.impl;

import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.NodeEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.impl.NodesManager;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NodeEventHandler implements NetWorkEventHandler {

    private static final NodeEventHandler INSTANCE = new NodeEventHandler();

    private NodesManager nodesManager;

    private NodeEventHandler() {

    }

    public static NodeEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseNetworkEvent networkEvent, Node node) {
        NodeEvent event = (NodeEvent) networkEvent;
        for (Node newNode : event.getEventBody().getNodes()) {
            newNode.setType(Node.OUT);
            newNode.setMessageHandlerFactory(node.getMessageHandlerFactory());
            nodesManager.addNodeToGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP, newNode);
        }
        return null;
    }

    public void setNodesManager(NodesManager nodesManager) {
        this.nodesManager = nodesManager;
    }
}
