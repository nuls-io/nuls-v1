package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2018/1/15
 */
public class BlockHashResponse extends BaseNulsData {

    private boolean best;

    private List<Long> heightList;

    private List<NulsDigestData> hashList;

    @Override
    public int size() {
        //best
        int size = 1;
        size += Utils.sizeOfSerialize(heightList.size());
        for (Long height : heightList) {
            size += Utils.sizeOfSerialize(height);
        }
        size += Utils.sizeOfSerialize(hashList.size());
        for (NulsDigestData hash : hashList) {
            size += Utils.sizeOfSerialize(hash);
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBoolean(best);
        stream.writeVarInt(heightList.size());
        for (Long height : heightList) {
            stream.writeVarInt(height);
        }
        stream.writeVarInt(hashList.size());
        for (NulsDigestData hash : hashList) {
            stream.writeNulsData(hash);
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        best = byteBuffer.readBoolean();
        int heightListSize = byteBuffer.readInt32LE();
        if (heightListSize > 0) {
            this.heightList = new ArrayList<>();
            for (int i = 0; i < heightListSize; i++) {
                heightList.add(byteBuffer.readVarInt());
            }
        }
        int hashListSize = byteBuffer.readInt32LE();
        if (hashListSize <= 0) {
            return;
        }
        this.hashList = new ArrayList<>();
        for (int i = 0; i < hashListSize; i++) {
            hashList.add(byteBuffer.readHash());
        }
    }

    public NulsDigestData getHash(){
        try {
            return NulsDigestData.calcDigestData(this.serialize())
        } catch (IOException e) {
            Log.error(e);
        }
        return null;
    }
}
