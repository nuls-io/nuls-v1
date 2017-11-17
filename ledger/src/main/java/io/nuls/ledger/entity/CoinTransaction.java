package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.utils.date.TimeService;

/**
 * Created by Niels on 2017/11/14.
 */
public abstract class CoinTransaction<T extends NulsData> extends Transaction {
    protected T txData;

    public CoinTransaction(){
        this.time = TimeService.currentTimeMillis();
    }

    public T getTxData() {
        return txData;
    }

    public void setTxData(T txData) {
        this.txData = txData;
    }
}
