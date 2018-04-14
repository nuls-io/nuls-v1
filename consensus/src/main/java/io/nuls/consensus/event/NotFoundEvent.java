package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.NotFound;
import io.nuls.core.event.NoticeData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author: Niels Wang
 * @date: 2018/4/9
 */
public class NotFoundEvent extends BaseConsensusEvent<NotFound> {
    public NotFoundEvent() {
        super(ConsensusEventType.NOT_FOUND_HASH);
    }

    @Override
    protected NotFound parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new NotFound());
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }
}
