package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class SmallBlockEvent extends BaseConsensusEvent {

    private List<Transaction> txList;

    public SmallBlockEvent() {
        super(ConsensusEventType.SMALL_BLOCK);
    }

    @Override
    protected Block parseEventBody(NulsByteBuffer byteBuffer) {
        Block block = new Block();
        block.parse(byteBuffer);
        return block;
    }


}
