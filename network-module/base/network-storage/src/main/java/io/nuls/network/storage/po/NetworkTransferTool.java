package io.nuls.network.storage.po;

import io.nuls.network.entity.Node;

public class NetworkTransferTool {

    public static NodePo toPojo(Node node) {
        NodePo po = new NodePo();
        po.setId(node.getId());
        po.setIp(node.getIp());
        po.setPort(node.getSeverPort());
        po.setLastTime(node.getLastTime());
        po.setLastFailTime(node.getLastFailTime());
        po.setFailCount(node.getFailCount());
        return po;
    }

    public static void toPojo(Node node, NodePo po) {
        po.setId(node.getId());
        po.setIp(node.getIp());
        po.setPort(node.getSeverPort());
        po.setLastTime(node.getLastTime());
        po.setLastFailTime(node.getLastFailTime());
        po.setFailCount(node.getFailCount());
    }

    public static Node toNode(NodePo po) {
        Node node = new Node();
        node.setId(po.getId());
        node.setIp(po.getIp());
        node.setPort(po.getPort());
        node.setSeverPort(po.getPort());
        node.setFailCount(0);
        node.setLastTime(po.getLastTime());
        node.setLastFailTime(po.getLastFailTime());
        return node;
    }
}
