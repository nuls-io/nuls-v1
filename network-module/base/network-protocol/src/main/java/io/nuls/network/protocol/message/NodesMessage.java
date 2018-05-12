package io.nuls.network.protocol.message;


import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;

public class NodesMessage extends BaseNetworkMessage<NodeMessageBody>{

    /**
     * 初始化基础消息的消息头
     */
    public NodesMessage() {
        super(NetworkConstant.NETWORK_GET_NODE);
    }

    @Override
    protected NodeMessageBody parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new NodeMessageBody());
    }

    public NodesMessage(NodeMessageBody body) {
        this();
        this.setMsgBody(body);
    }
}
