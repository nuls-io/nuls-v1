package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.NetworkDataHeader;

import java.io.IOException;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionEvent extends BaseNetworkEvent {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private BasicTypeData<Integer> externalPort;


    public GetVersionEvent(int externalPort) {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_GET_VERSION_MESSAGE);
        this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.externalPort = new BasicTypeData<>(externalPort);
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("getVersionData:{");
        buffer.append("version:" + version.getVersion() + ", ");
        buffer.append("externalPort:" + externalPort + "}");

        return buffer.toString();
    }

    public BasicTypeData<Integer> getExternalPort() {
        return externalPort;
    }

    public void setExternalPort(BasicTypeData<Integer> externalPort) {
        this.externalPort = externalPort;
    }

    @Override
    public Object copy() {
        return null;
    }
}
