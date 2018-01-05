package io.nuls.network.message.impl;

import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.GetNodeEvent;
import io.nuls.network.message.entity.NodeEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.impl.NodesManager;

import java.util.List;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class GetNodeEventHandler implements NetWorkEventHandler {

    private static final GetNodeEventHandler INSTANCE = new GetNodeEventHandler();

    private NodesManager nodesManager;

    private GetNodeEventHandler() {

    }

    public static GetNodeEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseNetworkEvent event, Node node) {
        GetNodeEvent getNodeEvent = (GetNodeEvent) event;

        List<Node> list = nodesManager.getAvailableNodes(getNodeEvent.getEventBody().getVal(), node);
        NodeEvent replyData = new NodeEvent(list);
        return new NetworkEventResult(true, replyData);
    }

    public void setNodesManager(NodesManager nodesManager) {
        this.nodesManager = nodesManager;
    }

}
