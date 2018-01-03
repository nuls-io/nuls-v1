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
public class PingData extends BaseNetworkData {
    public static final short OWN_MAIN_VERSION = 1;
    public static final short OWN_SUB_VERSION = 0001;

    public PingData() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION, NetworkConstant.NETWORK_PING_MESSAGE);
    }

    @Override
    public int size() {
        //todo
        return 0;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo

    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) {
        //todo

    }

}
