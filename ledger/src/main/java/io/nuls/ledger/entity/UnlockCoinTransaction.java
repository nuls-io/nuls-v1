package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.constant.TransactionConstant;

/**
 * Created by Niels on 2017/11/20.
 */
public abstract class UnlockCoinTransaction<T extends NulsData> extends CoinTransaction<T> {
    public UnlockCoinTransaction(){
        this.type = TransactionConstant.TX_TYPE_UNLOCK;
    }

}
