package io.nuls.network.message.impl;

import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.NodeMessageBody;
import io.nuls.network.protocol.message.NodesIpMessage;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GetNodesIpMessageHandler implements BaseNetworkMeesageHandler {

    private static GetNodesIpMessageHandler instance = new GetNodesIpMessageHandler();

    private GetNodesIpMessageHandler() {

    }

    public static GetNodesIpMessageHandler getInstance() {
        return instance;
    }

    private NodeManager nodeManager = NodeManager.getInstance();

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {
        Collection<Node> availableNodes = nodeManager.getNodes().values();
        List<String> ipList = new ArrayList<>();
        for (Node n : availableNodes) {
            ipList.add(n.getIp());
        }

        NodeMessageBody messageBody = new NodeMessageBody();
        messageBody.setIpList(ipList);
        NodesIpMessage nodesIpMessage = new NodesIpMessage(messageBody);

        return new NetworkEventResult(true, nodesIpMessage);
    }
}
