package io.nuls.network.protocol.message;


import io.nuls.network.constant.NetworkConstant;

public class HandshakeMessage extends BaseNetworkMessage<NetworkMessageBody> {
    /**
     * 初始化基础消息的消息头
     */
    public HandshakeMessage(NetworkMessageBody body) {
        super(NetworkConstant.NETWORK_HANDSHAKE);
        this.setMsgBody(body);
    }

}
