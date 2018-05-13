package io.nuls.network.message.impl;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.entity.NetworkEventResult;
import io.nuls.network.entity.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.GetVersionMessage;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;
import io.nuls.network.protocol.message.VersionMessage;
import io.nuls.protocol.message.base.BaseMessage;

public class GetVersionMessageHandler implements BaseNetworkMeesageHandler {

    private static GetVersionMessageHandler instance = new GetVersionMessageHandler();

    private GetVersionMessageHandler() {

    }

    public static GetVersionMessageHandler getInstance() {
        return instance;
    }

    private NodeManager nodeManager = NodeManager.getInstance();

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {

        GetVersionMessage getVersionMessage = (GetVersionMessage) message;

        NetworkMessageBody body = getVersionMessage.getMsgBody();

        if (body.getBestBlockHeight() < 0) {
            node.setStatus(Node.BAD);
            nodeManager.removeNode(node.getId());
            return null;
        }
        node.setBestBlockHeight(body.getBestBlockHeight());
        node.setBestBlockHash(body.getBestBlockHash());

        NetworkMessageBody myVersionBody = new NetworkMessageBody(NetworkConstant.HANDSHAKE_CLIENT_TYPE, NetworkParam.getInstance().getPort(),
                10001, NulsDigestData.calcDigestData("a1b2c3d4e5gf6g7h8i9j10".getBytes()));
        return new NetworkEventResult(true, new VersionMessage(myVersionBody));
    }

}
