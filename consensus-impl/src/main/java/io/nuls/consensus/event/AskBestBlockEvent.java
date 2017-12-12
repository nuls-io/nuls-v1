package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class AskBestBlockEvent extends BaseConsensusEvent {
    public AskBestBlockEvent( ) {
        super(PocConsensusConstant.EVENT_TYPE_ASK_BEST_BLOCK);
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) {
        // todo auto-generated method stub(niels)
        return null;
    }
}
