package io.nuls.consensus.poc.storage.po;

import io.nuls.kernel.model.BlockHeader;

/**
 * @author: Charlie
 * @date: 2018/9/4
 */
public class EvidencePo {

    private long roundIndex;
    private BlockHeader blockHeader1;
    private BlockHeader blockHeader2;

    public EvidencePo(){

    }

    public EvidencePo(long roundIndex, BlockHeader blockHeader1, BlockHeader blockHeader2){
        this.roundIndex = roundIndex;
        this.blockHeader1 = blockHeader1;
        this.blockHeader2 = blockHeader2;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public BlockHeader getBlockHeader1() {
        return blockHeader1;
    }

    public void setBlockHeader1(BlockHeader blockHeader1) {
        this.blockHeader1 = blockHeader1;
    }

    public BlockHeader getBlockHeader2() {
        return blockHeader2;
    }

    public void setBlockHeader2(BlockHeader blockHeader2) {
        this.blockHeader2 = blockHeader2;
    }
}
