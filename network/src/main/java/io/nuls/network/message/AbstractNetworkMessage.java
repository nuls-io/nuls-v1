package io.nuls.network.message;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.entity.VersionMessage;

public abstract class AbstractNetworkMessage extends BaseNulsData {

    protected short type;

    public static AbstractNetworkMessage transfer(Short msgType, byte[] data) {
        AbstractNetworkMessage message = null;
        switch (msgType) {
            case NetworkConstant.Network_Version_Message:
                message = new VersionMessage();
                break;
            default:
        }
        message.parse(new NulsByteBuffer(data));
        return message;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }
}
