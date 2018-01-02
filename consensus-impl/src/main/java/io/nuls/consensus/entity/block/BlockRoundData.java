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

    protected long roundIndex;

    protected int packingIndexOfRound;

    public long getRoundEndTime() {
        return roundStartTime + consensusMemberCount * PocConsensusConstant.BLOCK_TIME_INTERVAL * 1000L;
    }

    public BlockRoundData(byte[] extend) throws NulsException {
        this.parse(new NulsByteBuffer(extend));
    }

    public BlockRoundData() {
    }

    @Override
    public int size() {
        int size = 0;
        size += VarInt.sizeOf(roundIndex);
        size += VarInt.sizeOf(consensusMemberCount);
        size += VarInt.sizeOf(roundStartTime);
        size += VarInt.sizeOf(packingIndexOfRound);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(roundIndex);
        stream.writeVarInt(consensusMemberCount);
        stream.writeVarInt(roundStartTime);
        stream.writeVarInt(packingIndexOfRound);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.roundIndex = byteBuffer.readVarInt();
        this.consensusMemberCount = (int) byteBuffer.readVarInt();
        this.roundStartTime = byteBuffer.readVarInt();
        this.packingIndexOfRound = (int) byteBuffer.readVarInt();
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

    public int getPackingIndexOfRound() {
        return packingIndexOfRound;
    }

    public void setPackingIndexOfRound(int packingIndexOfRound) {
        this.packingIndexOfRound = packingIndexOfRound;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }
}
