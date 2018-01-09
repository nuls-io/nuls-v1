package io.nuls.ledger.event;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.manager.TransactionManager;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.constant.LedgerConstant;

/**
 * @author Niels
 * @date 2017/11/8
 */
public class TransactionEvent extends io.nuls.core.event.BaseEvent<Transaction> {

    public TransactionEvent() {
        super(NulsConstant.MODULE_ID_LEDGER, LedgerConstant.EVENT_TYPE_TRANSACTION);
    }

    @Override
    protected Transaction parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        try {
            return TransactionManager.getInstance(byteBuffer);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
}
