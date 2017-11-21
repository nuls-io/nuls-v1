package io.nuls.network.message;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.entity.VersionMessage;

public abstract class NetworkMessage extends NulsData {

    protected short msgType;

    public static NetworkMessage transfer(Short msgType, byte[] data) {
        NetworkMessage message = null;
        switch (msgType) {
            case NetworkConstant.Network_Version_Message:
                message = new VersionMessage();
                break;
        }
        message.parse(new NulsByteBuffer(data));
        return message;
    }

    public short getMsgType() {
        return msgType;
    }

    public void setMsgType(short msgType) {
        this.msgType = msgType;
    }
}
