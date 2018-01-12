package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.TxHashData;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * get block by height.
 *
 * @author Niels
 * @date 2017/11/13
 */
public class GetTxGroupRequest extends BaseConsensusEvent<TxHashData> {


    public GetTxGroupRequest() {
        super(ConsensusEventType.GET_TX_GROUP);
    }


    @Override
    protected TxHashData parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return byteBuffer.readNulsData(new TxHashData());
    }

}