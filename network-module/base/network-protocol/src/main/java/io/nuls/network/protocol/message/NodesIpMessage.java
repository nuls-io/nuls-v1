package io.nuls.network.protocol.message;


import io.nuls.network.constant.NetworkConstant;

public class NodesIpMessage extends BaseNetworkMessage<NodeMessageBody>{

    /**
     * 初始化基础消息的消息头
     */
    public NodesIpMessage() {
        super(NetworkConstant.NETWORK_GET_NODE);
    }

    public NodesIpMessage(NodeMessageBody body) {
        this();
        this.setMsgBody(body);
    }
}
