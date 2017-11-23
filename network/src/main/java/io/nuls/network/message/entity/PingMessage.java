package io.nuls.network.message.entity;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.AbstractNetworkMessage;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class PingMessage extends AbstractNetworkMessage {
    public static final short OWN_MAIN_VERSION = 1;
    public static final short OWN_SUB_VERSION = 0001;

    public PingMessage() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.type = NetworkConstant.NETWORK_PING_MESSAGE;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {

    }

}
