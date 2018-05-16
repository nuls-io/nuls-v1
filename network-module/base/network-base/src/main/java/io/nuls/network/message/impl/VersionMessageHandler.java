package io.nuls.network.message.impl;

import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.NetworkMessageBody;
import io.nuls.network.protocol.message.VersionMessage;
import io.nuls.protocol.message.base.BaseMessage;

public class VersionMessageHandler implements BaseNetworkMeesageHandler {

    private static VersionMessageHandler instance = new VersionMessageHandler();

    private VersionMessageHandler() {

    }

    public static VersionMessageHandler getInstance() {
        return instance;
    }

    private NodeManager nodeManager = NodeManager.getInstance();

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {
        VersionMessage versionMessage = (VersionMessage) message;
        NetworkMessageBody body = versionMessage.getMsgBody();

        if (body.getBestBlockHeight() < 0) {
            node.setStatus(Node.BAD);
            nodeManager.removeNode(node.getId());
            return null;
        }
        node.setBestBlockHeight(body.getBestBlockHeight());
        node.setBestBlockHash(body.getBestBlockHash());
        return null;
    }
}
