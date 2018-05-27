package io.nuls.network.message.impl;

import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.GetNodesMessage;
import io.nuls.network.protocol.message.NodeMessageBody;
import io.nuls.network.protocol.message.NodesMessage;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.*;

public class GetNodesMessageHandler implements BaseNetworkMeesageHandler {

    private static GetNodesMessageHandler instance = new GetNodesMessageHandler();

    private GetNodesMessageHandler() {

    }

    public static GetNodesMessageHandler getInstance() {
        return instance;
    }


    private NodeManager nodeManager = NodeManager.getInstance();

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {
        GetNodesMessage getNodesMessage = (GetNodesMessage) message;

        NodeMessageBody body = getNodesMessage.getMsgBody();
        body.getIpList().add(node.getIp());
        List<Node> nodeList = getAvailableNodes(body.getLength(), body.getIpList());
        body = new NodeMessageBody();
        body.setNodeList(nodeList);
        NodesMessage nodesMessage = new NodesMessage(body);
        return new NetworkEventResult(true, nodesMessage);
    }

    private List<Node> getAvailableNodes(int length, List<String> ipList) {
        List<Node> nodeList = new ArrayList<>();
        List<Node> availableNodes = new ArrayList<>(nodeManager.getAvailableNodes());
        Collections.shuffle(availableNodes);
        Set<String> ipSet = new HashSet<>();
        ipSet.addAll(ipList);
        for (Node node : availableNodes) {
            if (ipSet.contains(node.getIp())) {
                continue;
            }
            if (node.getSeverPort() == null || node.getSeverPort() == 0) {
                continue;
            }
            Node newNode = new Node();
            newNode.setIp(node.getIp());
            newNode.setPort(node.getSeverPort());
            newNode.setSeverPort(node.getSeverPort());
            ipSet.add(node.getIp());
            nodeList.add(newNode);
            if (nodeList.size() == length) {
                break;
            }
        }
        return nodeList;
    }
}
