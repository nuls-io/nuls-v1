package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author win10
 * @date 2017/10/30
 */
public class NulsBlock extends Block {

    /**
     * the count of the agents the current round
     */
    private int countOfRound;
    private long roundStartTime;
    private int orderInRound;

    @Override
    public int size() {
        fillExtendBytes();
        return super.size();
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        fillExtendBytes();
        super.serializeToStream(stream);
    }

    private void fillExtendBytes() {
        byte[] bytes1 = new VarInt(countOfRound).encode();
        byte[] bytes2 = new VarInt(roundStartTime).encode();
        byte[] bytes3 = new VarInt(orderInRound).encode();
        byte[] extend = new byte[bytes1.length+bytes2.length+bytes3.length];
        this.setExtend(extend);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        super.parse(byteBuffer);
        NulsByteBuffer extendBuffer = new NulsByteBuffer(this.getExtend());
        countOfRound = (int) extendBuffer.readVarInt();
        roundStartTime = extendBuffer.readVarInt();
        orderInRound = (int) extendBuffer.readVarInt();
    }

    public int getCountOfRound() {
        return countOfRound;
    }

    public void setCountOfRound(int countOfRound) {
        this.countOfRound = countOfRound;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public int getOrderInRound() {
        return orderInRound;
    }

    public void setOrderInRound(int orderInRound) {
        this.orderInRound = orderInRound;
    }
}
