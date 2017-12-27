package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/18
 */
public class AskSmallBlockData extends BaseNulsData {

    private NulsDigestData blockHash;

    private List<NulsDigestData> txHashList;

    @Override
    public int size() {
        int size = 0;
        size += blockHash.size();
        size += VarInt.sizeOf(txHashList.size());
        size += this.getTxHashBytesLength();
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        blockHash.serializeToStream(stream);
        stream.writeVarInt(txHashList.size());
        for(NulsDigestData data:txHashList){
            data.serializeToStream(stream);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.blockHash = new NulsDigestData();
        this.blockHash.parse(byteBuffer);
       long txCount = byteBuffer.readVarInt();
       this.txHashList = new ArrayList<>();
       for(int i=0;i<txCount;i++){
           NulsDigestData data = new NulsDigestData();
           data.parse(byteBuffer);
           this.txHashList.add(data);
       }
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
}
