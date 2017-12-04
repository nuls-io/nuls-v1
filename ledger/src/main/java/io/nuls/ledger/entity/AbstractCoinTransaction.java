package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.date.TimeService;

/**
 *
 * @author Niels
 * @date 2017/11/14
 */
public abstract class AbstractCoinTransaction<T extends BaseNulsData> extends Transaction {
    protected T txData;

    public AbstractCoinTransaction(int type){
        super(type);
    }

    public T getTxData() {
        return txData;
    }

    public void setTxData(T txData) {
        this.txData = txData;
    }
}
