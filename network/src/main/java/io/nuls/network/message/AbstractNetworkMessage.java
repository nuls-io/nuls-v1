package io.nuls.network.message;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.entity.VersionMessage;

/**
 * @author vivi
 * @date 2017/11/21
 */
public abstract class AbstractNetworkMessage extends BaseNulsData {

    protected short type;

    public AbstractNetworkMessage(short mainVersion, short subVersion) {
        super(mainVersion, subVersion);
    }

    public static AbstractNetworkMessage transfer(Short msgType, byte[] data) {
        AbstractNetworkMessage message = null;
        switch (msgType) {
            case NetworkConstant.NETWORK_VERSION_MESSAGE:
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
