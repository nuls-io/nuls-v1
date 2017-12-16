package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.tx.ExitConsensusTransaction;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/7
 */
//todo
public class ExitConsensusEvent extends BaseConsensusEvent<ExitConsensusTransaction> {

    public ExitConsensusEvent() {
        super(ConsensusEventType.EXIT);
    }

    @Override
    protected ExitConsensusTransaction parseEventBody(NulsByteBuffer byteBuffer) {
        try {
            return (ExitConsensusTransaction) TransactionManager.getInstance(byteBuffer);
        } catch (IllegalAccessException e) {
            Log.error(e);
        } catch (InstantiationException e) {
            Log.error(e);
        }
        return null;
    }

}
