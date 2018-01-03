package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.AskTxGroupData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * get block by height.
 *
 * @author Niels
 * @date 2017/11/13
 */
public class GetTxGroupEvent extends BaseConsensusEvent<AskTxGroupData> {


    public GetTxGroupEvent() {
        super(ConsensusEventType.GET_SMALL_BLOCK);
    }


    @Override
    protected AskTxGroupData parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new AskTxGroupData());
    }

}