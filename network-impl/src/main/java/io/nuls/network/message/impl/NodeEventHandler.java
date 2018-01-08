package io.nuls.network.message.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkCacheService;
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

    private NetworkCacheService cacheService;

    private NodeEventHandler() {
        cacheService = NetworkCacheService.getInstance();
    }

    public static NodeEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseEvent networkEvent, Node node) {
        NodeEvent event = (NodeEvent) networkEvent;

        String key = event.getHeader().getEventType() + "-" + node.getIp() + "-" + node.getPort();
        if (cacheService.existEvent(key)) {
            node.destroy();
            return null;
        }
        cacheService.putEvent(key, event, false);

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
