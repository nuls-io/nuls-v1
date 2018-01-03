package io.nuls.core.chain.entity;

import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2018/1/2
 */
public class SmallBlock extends BaseNulsData {
    private NulsDigestData blockHash;
    private int txCount;
    private List<NulsDigestData> txHashList;

    @Override
    public int size() {
        int size = Utils.sizeOfSerialize(blockHash);
        size += Utils.sizeOfSerialize(txCount);
        for (NulsDigestData hash : txHashList) {
            size += Utils.sizeOfSerialize(hash);
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(blockHash);
        stream.writeVarInt(txCount);
        for (NulsDigestData hash : txHashList) {
            stream.writeNulsData(hash);
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        blockHash = byteBuffer.readHash();
        txCount = (int) byteBuffer.readVarInt();
        List<NulsDigestData> hashList = new ArrayList<>();
        for (int i = 0; i < txCount; i++) {
            hashList.add(byteBuffer.readHash());
        }
        if (!hashList.isEmpty()) {
            this.txHashList = hashList;
        }
    }

    public NulsDigestData getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(NulsDigestData blockHash) {
        this.blockHash = blockHash;
    }

    public List<NulsDigestData> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<NulsDigestData> txHashList) {
        this.txHashList = txHashList;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }
}
