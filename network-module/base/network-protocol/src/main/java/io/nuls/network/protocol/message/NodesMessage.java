package io.nuls.network.protocol.message;


import io.nuls.network.constant.NetworkConstant;

public class NodesMessage extends BaseNetworkMessage<NodeMessageBody>{

    /**
     * 初始化基础消息的消息头
     */
    public NodesMessage() {
        super(NetworkConstant.NETWORK_GET_NODE);
    }

    public NodesMessage(NodeMessageBody body) {
        this();
        this.setMsgBody(body);
    }
}
