package io.nuls.network.protocol.message;


import io.nuls.network.constant.NetworkConstant;

public class VersionMessage extends BaseNetworkMessage<NetworkMessageBody>{

    /**
     * 初始化基础消息的消息头
     */
    public VersionMessage() {
        super(NetworkConstant.NETWORK_VERSION);
    }

    public VersionMessage(NetworkMessageBody body) {
        this();
        this.setMsgBody(body);
    }
}
