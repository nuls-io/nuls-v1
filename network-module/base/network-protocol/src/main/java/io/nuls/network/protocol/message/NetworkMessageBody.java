package io.nuls.network.protocol.message;

import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.protostuff.Tag;

import java.io.IOException;

public class NetworkMessageBody extends BaseNulsData {


    private int handshakeType;

    private int severPort;

    private long bestBlockHeight;

    private NulsDigestData bestBlockHash;


    public NetworkMessageBody() {

    }

    public NetworkMessageBody(int handshakeType, int severPort, long bestBlockHeight, NulsDigestData bestBlockHash) {
        this.handshakeType = handshakeType;
        this.severPort = severPort;
        this.bestBlockHeight = bestBlockHeight;
        this.bestBlockHash = bestBlockHash;
    }

    public int getHandshakeType() {
        return handshakeType;
    }

    public void setHandshakeType(int handshakeType) {
        this.handshakeType = handshakeType;
    }

    public int getSeverPort() {
        return severPort;
    }

    public void setSeverPort(int severPort) {
        this.severPort = severPort;
    }

    public long getBestBlockHeight() {
        return bestBlockHeight;
    }

    public void setBestBlockHeight(long bestBlockHeight) {
        this.bestBlockHeight = bestBlockHeight;
    }

    public NulsDigestData getBestBlockHash() {
        return bestBlockHash;
    }

    public void setBestBlockHash(NulsDigestData bestBlockHash) {
        this.bestBlockHash = bestBlockHash;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        // todo auto-generated method stub

    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        // todo auto-generated method stub

    }

    @Override
    public int size() {
        // todo auto-generated method stub
        return 0;
    }
}
