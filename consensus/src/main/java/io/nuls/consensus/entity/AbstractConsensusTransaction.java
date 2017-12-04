package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class AbstractConsensusTransaction<T extends BaseNulsData> extends Transaction {

    private T txData;

    public AbstractConsensusTransaction(int type) {
        super(type);
    }

    public T getTxData() {
        return txData;
    }

    public void setTxData(T txData) {
        this.txData = txData;
    }
}