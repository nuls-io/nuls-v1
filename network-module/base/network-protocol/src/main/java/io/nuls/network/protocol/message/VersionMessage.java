package io.nuls.network.protocol.message;


import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;

public class VersionMessage extends BaseNetworkMessage<NetworkMessageBody>{

    /**
     * 初始化基础消息的消息头
     */
    public VersionMessage() {
        super(NetworkConstant.NETWORK_VERSION);
    }

    @Override
    protected NetworkMessageBody parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new NetworkMessageBody());
    }

    public VersionMessage(NetworkMessageBody body) {
        this();
        this.setMsgBody(body);
    }
}
