package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.TransactionConstant;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class UnlockCoinTransaction<T extends BaseNulsData> extends AbstractCoinTransaction<T> {
    public UnlockCoinTransaction() {
        super(TransactionConstant.TX_TYPE_UNLOCK);
    }

}
