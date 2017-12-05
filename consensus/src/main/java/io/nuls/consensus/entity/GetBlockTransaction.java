package io.nuls.consensus.entity;

import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class GetBlockTransaction extends AbstractConsensusTransaction {
    private long height;

    public GetBlockTransaction() {
        super(ConsensusConstant.TX_TYPE_GET_BLOCK);
    }

    @Override
    protected int dataSize() {
        int size = super.dataSize();
        size += VarInt.sizeOf(height);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        stream.writeVarInt(height);
    }

    @Override
    protected void parseObject(NulsByteBuffer byteBuffer) {
        super.parseObject(byteBuffer);
        this.height = byteBuffer.readVarInt();
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

}
