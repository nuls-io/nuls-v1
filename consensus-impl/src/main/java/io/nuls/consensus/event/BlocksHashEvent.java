package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.BlockHashResponse;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2018/1/15
 */
public class BlocksHashEvent extends BaseEvent<BlockHashResponse> {

    public BlocksHashEvent() {
        super(NulsConstant.MODULE_ID_CONSENSUS, PocConsensusConstant.EVENT_TYPE_BLOCKS_HASH);
    }

    @Override
    protected BlockHashResponse parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new BlockHashResponse());
    }
}
