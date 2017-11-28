package io.nuls.network.message;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.entity.GetVersionData;
import io.nuls.network.message.entity.VersionData;

/**
 * @author vivi
 * @date 2017/11/21
 */
public abstract class BaseNetworkData extends BaseNulsData {

    protected NetworkDataHeader networkHeader;

    public BaseNetworkData(short mainVersion, short subVersion) {
        super(mainVersion, subVersion);
    }

    public BaseNetworkData(short mainVersion, short subVersion, NetworkDataHeader networkHeader) {
        super(mainVersion, subVersion);
        this.networkHeader = networkHeader;
    }

    public static BaseNetworkData transfer(Short msgType, byte[] data) {
        BaseNetworkData message = null;
        switch (msgType) {
            case NetworkConstant.NETWORK_GET_VERSION_MESSAGE:
                message = new GetVersionData();
                break;
            case NetworkConstant.NETWORK_VERSION_MESSAGE:
                message = new VersionData();
                break;

            default:
        }
        message.parse(new NulsByteBuffer(data));
        return message;
    }

    public NetworkDataHeader getNetworkHeader() {
        return networkHeader;
    }

    public void setNetworkHeader(NetworkDataHeader networkHeader) {
        this.networkHeader = networkHeader;
    }
}
