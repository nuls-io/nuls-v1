package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.SmallBlockData;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class SmallBlockEvent extends BaseConsensusEvent<SmallBlockData> {

    public SmallBlockEvent() {
        super(ConsensusEventType.SMALL_BLOCK);
    }

    @Override
    protected SmallBlockData parseEventBody(NulsByteBuffer byteBuffer) {
        SmallBlockData data = new SmallBlockData();
        data.parse(byteBuffer);
        return data;
    }


}
