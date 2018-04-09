package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.event.NoticeData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author: Niels Wang
 * @date: 2018/4/9
 */
public class BlockNotFoundEvent extends BaseConsensusEvent<NulsDigestData> {
    public BlockNotFoundEvent() {
        super(ConsensusEventType.NOT_FOUND_HASH);
    }

    @Override
    protected NulsDigestData parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readHash();
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }
}
