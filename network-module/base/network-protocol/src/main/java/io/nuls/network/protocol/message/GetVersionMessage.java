package io.nuls.network.protocol.message;


import io.nuls.network.constant.NetworkConstant;

public class GetVersionMessage extends BaseNetworkMessage<NetworkMessageBody>{

    /**
     * 初始化基础消息的消息头
     */
    public GetVersionMessage() {
        super(NetworkConstant.NETWORK_GET_VERSION);
    }

    public GetVersionMessage(NetworkMessageBody body) {
        this();
        this.setMsgBody(body);
    }
}
