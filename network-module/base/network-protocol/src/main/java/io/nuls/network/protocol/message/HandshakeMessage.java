package io.nuls.network.protocol.message;


import io.nuls.network.constant.NetworkConstant;

public class HandshakeMessage extends BaseNetworkMessage<NetworkMessageBody> {
    /**
     * 初始化基础消息的消息头
     */

    public HandshakeMessage() {
        super(NetworkConstant.NETWORK_HANDSHAKE);
    }

    public HandshakeMessage(NetworkMessageBody body) {
        this();
        this.setMsgBody(body);
    }

}
