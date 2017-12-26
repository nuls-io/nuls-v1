package io.nuls.consensus.entity.block;

import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/26
 */
public class BlockData {
    private long time;
    private long height;
    private NulsDigestData preHash;
    private List<Transaction> txList;
    private BlockRoundData roundData;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public NulsDigestData getPreHash() {
        return preHash;
    }

    public void setPreHash(NulsDigestData preHash) {
        this.preHash = preHash;
    }

    public List<Transaction> getTxList() {
        return txList;
    }

    public void setTxList(List<Transaction> txList) {
        this.txList = txList;
    }

    public BlockRoundData getRoundData() {
        return roundData;
    }

    public void setRoundData(BlockRoundData roundData) {
        this.roundData = roundData;
    }
}
