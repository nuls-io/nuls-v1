package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.TxHashData;
import io.nuls.core.chain.entity.SmallBlock;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 *
 * @author Niels
 * @date 2017/11/13
 */
public class SmallBlockEvent extends BaseConsensusEvent<SmallBlock> {


    public SmallBlockEvent() {
        super(ConsensusEventType.SMALL_BLOCK);
    }

    @Override
    protected SmallBlock parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new SmallBlock());
    }

}