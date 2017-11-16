package io.nuls.ledger.entity;

import io.nuls.core.constant.TransactionConstant;

import java.util.List;

/**
 * Created by Niels on 2017/11/14.
 */
public class LockTransaction extends NulsTransaction {

    protected List<TransactionInput> inputs;
    protected List<TransactionOutput> outputs;

    public LockTransaction(){
        this.type = TransactionConstant.TX_TYPE_LOCK;
    }
}
