package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class AskBlockInfoEvent extends BaseConsensusEvent<BasicTypeData<Long>> {
    public AskBlockInfoEvent() {
        super(PocConsensusConstant.EVENT_TYPE_ASK_BLOCK);
    }

    public AskBlockInfoEvent(long height) {
        this();
        this.setEventBody(new BasicTypeData<>(height));
    }

    @Override
    protected BasicTypeData<Long> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        if (byteBuffer.isFinished()) {
            return null;
        }
        return new BasicTypeData(byteBuffer);
    }

}
