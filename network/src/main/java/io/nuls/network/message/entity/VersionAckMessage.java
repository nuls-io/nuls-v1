package io.nuls.network.message.entity;

import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.AbstractNetworkMessage;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class VersionAckMessage extends AbstractNetworkMessage {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 0001;

    private long bestBlockHeight;

    private String bestBlockHash;

    private String nulsVersion;

    public VersionAckMessage() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.type = NetworkConstant.NETWORK_VERSION_MESSAGE;
        this.nulsVersion = NulsContext.nulsVersion;
    }

    public VersionAckMessage(long bestBlockHeight, String bestBlockHash) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.type = NetworkConstant.NETWORK_VERSION_MESSAGE;
        this.bestBlockHash = bestBlockHash;
        this.bestBlockHeight = bestBlockHeight;
        this.nulsVersion = NulsContext.nulsVersion;
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
