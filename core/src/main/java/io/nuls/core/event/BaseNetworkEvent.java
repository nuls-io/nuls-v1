package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseNetworkEvent<T extends BaseNulsData> extends BaseEvent<T> {
    private NulsDigestData hash;
    private NulsSignData sign;

    public BaseNetworkEvent(short moduleId, short eventType, byte[] extend) {
        super(moduleId, eventType, extend);
    }

    public BaseNetworkEvent(short moduleId, short eventType) {
        this(moduleId, eventType, null);
    }

    @Override
    public final int size() {
        int size = super.size();
        size += Utils.sizeOfSerialize(hash);
        size += Utils.sizeOfSerialize(sign);
        return size;
    }

    @Override
    public final void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        stream.writeNulsData(hash);
        stream.writeNulsData(sign);
    }

    @Override
    public final void parse(NulsByteBuffer byteBuffer) throws NulsException {
        super.parse(byteBuffer);
        this.hash = byteBuffer.readHash();
        this.sign = byteBuffer.readSign();
    }

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public NulsSignData getSign() {
        return sign;
    }

    public void setSign(NulsSignData sign) {
        this.sign = sign;
    }
}
