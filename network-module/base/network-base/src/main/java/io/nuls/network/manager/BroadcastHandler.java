package io.nuls.network.manager;

import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.protocol.message.base.BaseMessage;

@Component
public class BroadcastHandler {

    private NetworkParam networkParam = NetworkParam.getInstance();

    @Autowired
    private NodeManager nodeManager;

    public BroadcastResult broadcast(BaseMessage msg, boolean asyn) {
        return null;
    }

    public BroadcastResult broadcastToNode(BaseMessage msg, String nodeId, boolean asyn) {
        return null;
    }

    public BroadcastResult broadcastToNode(BaseMessage msg, Node node, boolean asyn) {
        return null;
    }
}
