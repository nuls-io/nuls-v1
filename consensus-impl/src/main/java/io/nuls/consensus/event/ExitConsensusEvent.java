package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.tx.PocExitConsensusTransaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class ExitConsensusEvent extends BaseConsensusEvent<PocExitConsensusTransaction> {

    public ExitConsensusEvent() {
        super( PocConsensusConstant.EVENT_TYPE_EXIT_CONSENSUS);
    }

    @Override
    protected PocExitConsensusTransaction parseEventBody(NulsByteBuffer byteBuffer) {
        try {
            return (PocExitConsensusTransaction) TransactionManager.getInstance(byteBuffer);
        } catch (IllegalAccessException e) {
            Log.error(e);
        } catch (InstantiationException e) {
            Log.error(e);
        }
        return null;
    }

}
