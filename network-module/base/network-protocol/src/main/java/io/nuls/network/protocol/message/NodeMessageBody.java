package io.nuls.network.protocol.message;

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.network.entity.Node;
import io.protostuff.Tag;

import java.util.List;

public class NodeMessageBody extends BaseNulsData{

    @Tag(1)
    private int length;
    @Tag(2)
    private List<String> ipList;
    @Tag(3)
    private List<Node> nodeList;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }
}
