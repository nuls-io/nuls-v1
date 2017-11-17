package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.chain.entity.Transaction;

/**
 * Created by Niels on 2017/11/14.
 */
public abstract class CoinTransaction<T extends NulsData> extends Transaction {
    protected T txData;

    public T getTxData() {
        return txData;
    }

    public void setTxData(T txData) {
        this.txData = txData;
    }
}
