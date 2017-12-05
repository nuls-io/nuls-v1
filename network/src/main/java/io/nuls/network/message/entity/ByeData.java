package io.nuls.network.message.entity;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.BaseNetworkData;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class ByeData extends BaseNetworkData {
    public static final short OWN_MAIN_VERSION = 1;
    public static final short OWN_SUB_VERSION = 0001;

    public ByeData() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION, NetworkConstant.NETWORK_BYE_MESSAGE);
    }

    @Override
    protected int dataSize() {
        //todo
        return 0;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo
    }

    @Override
    protected void parseObject(NulsByteBuffer byteBuffer) {
        //todo

    }

}
