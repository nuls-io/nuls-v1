package io.nuls.consensus.entity;

import io.nuls.consensus.constant.NotFoundType;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author: Niels Wang
 * @date: 2018/4/9
 */
public class NotFound extends BaseNulsData {

    private NotFoundType type;
    private NulsDigestData hash;

    public NotFound() {
    }

    public NotFound(NotFoundType type, NulsDigestData hash) {
        this.type = type;
        this.hash = hash;
    }

    @Override
    public int size() {
        int size = 1;
        size += Utils.sizeOfNulsData(hash);
        return size;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write((byte) type.getCode());
        stream.writeNulsData(hash);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.type = NotFoundType.getType(byteBuffer.readByte());
        this.hash = byteBuffer.readHash();
    }

    public NotFoundType getType() {
        return type;
    }

    public void setType(NotFoundType type) {
        this.type = type;
    }

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }
}
