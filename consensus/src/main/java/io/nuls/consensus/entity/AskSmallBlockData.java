package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/18
 */
public class AskSmallBlockData extends BaseNulsData {

    private long height;

    private long txCount;

    private List<NulsDigestData> txHashList;

    @Override
    public int size() {
        int size = 0;
        size += VarInt.sizeOf(height);
        size += VarInt.sizeOf(txCount);
        size += this.getTxHashBytesLength();
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        // todo auto-generated method stub(niels)

    }

    private byte[] getTxHashListBytes() throws IOException {
        if (null == txHashList) {
            return null;
        }
        int size = this.getTxHashBytesLength();
        byte[] bytes = new byte[size];
        int index = 0;
        for (NulsDigestData hash : txHashList) {
            int hashSize = hash.size();
            System.arraycopy(hash.serialize(), 0, bytes, index, hashSize);
            index += hashSize;
        }
        return bytes;
    }

    private int getTxHashBytesLength(){
        int size = 0;
        for (NulsDigestData hash : txHashList) {
            size += hash.size();
        }
        return size;
    }
    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getTxCount() {
        return txCount;
    }

    public void setTxCount(long txCount) {
        this.txCount = txCount;
    }

    public List<NulsDigestData> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<NulsDigestData> txHashList) {
        this.txHashList = txHashList;
    }
}
