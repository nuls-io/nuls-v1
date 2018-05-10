package io.nuls.network.protocol.message;


import io.nuls.network.constant.NetworkConstant;

public class GetNodesMessage extends BaseNetworkMessage<NodeMessageBody>{

    /**
     * 初始化基础消息的消息头
     */
    public GetNodesMessage() {
        super(NetworkConstant.NETWORK_GET_NODE);
    }

    public GetNodesMessage(NodeMessageBody body) {
        this();
        this.setMsgBody(body);
    }
}
