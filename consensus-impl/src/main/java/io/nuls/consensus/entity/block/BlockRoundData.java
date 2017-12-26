package io.nuls.consensus.entity.block;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/25
 */
public class BlockRoundData extends BaseNulsData {

    protected int consensusMemberCount;

    protected long roundStartTime;

    protected int packingIndex;

    public long getRoundEndTime(){
        return roundStartTime+consensusMemberCount* PocConsensusConstant.BLOCK_TIME_INTERVAL*1000L;
    }

    public BlockRoundData(byte[] extend) throws NulsException {
        this.parse(new NulsByteBuffer(extend));
    }
    public BlockRoundData( )   {
    }

    @Override
    public int size() {
        int size = 0;
        size += VarInt.sizeOf(consensusMemberCount);
        size += VarInt.sizeOf(roundStartTime);
        size += VarInt.sizeOf(packingIndex);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(consensusMemberCount);
        stream.writeVarInt(roundStartTime);
        stream.writeVarInt(packingIndex);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.consensusMemberCount = (int) byteBuffer.readVarInt();
        this.roundStartTime = (int) byteBuffer.readVarInt();
        this.packingIndex = (int) byteBuffer.readVarInt();
    }

    public int getConsensusMemberCount() {
        return consensusMemberCount;
    }

    public void setConsensusMemberCount(int consensusMemberCount) {
        this.consensusMemberCount = consensusMemberCount;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public int getPackingIndex() {
        return packingIndex;
    }

    public void setPackingIndex(int packingIndex) {
        this.packingIndex = packingIndex;
    }
}
