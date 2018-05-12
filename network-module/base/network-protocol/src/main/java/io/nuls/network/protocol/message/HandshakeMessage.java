package io.nuls.network.protocol.message;


import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;

public class HandshakeMessage extends BaseNetworkMessage<NetworkMessageBody> {
    /**
     * 初始化基础消息的消息头
     */

    public HandshakeMessage() {
        super(NetworkConstant.NETWORK_HANDSHAKE);
    }

    @Override
    protected NetworkMessageBody parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        NetworkMessageBody messageBody = new NetworkMessageBody();
        messageBody.parse(byteBuffer);
        return messageBody;
    }

    public HandshakeMessage(NetworkMessageBody body) {
        this();
        this.setMsgBody(body);
    }

}
