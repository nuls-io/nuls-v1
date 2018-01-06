package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionEvent extends BaseNetworkEvent<BasicTypeData<Integer>> {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    public GetVersionEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_GET_VERSION_MESSAGE);
    }

    public GetVersionEvent(int externalPort) {
        this();
//        this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.setEventBody(new BasicTypeData(externalPort));
    }

    @Override
    protected BasicTypeData<Integer> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new BasicTypeData<>());
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("getVersionData:{");
        buffer.append("version:" + version.getVersion() + ", ");

        return buffer.toString();
    }
}
