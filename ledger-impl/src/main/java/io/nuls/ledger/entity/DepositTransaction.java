package io.nuls.ledger.entity;

import io.nuls.core.constant.TransactionConstant;

/**
 * Created by Niels on 2017/11/17.
 */
public class DepositTransaction extends UtxoCoinTransaction {
    public DepositTransaction() {
        this.type = TransactionConstant.TX_TYPE_DEPOSIT;
    }
}
