package io.nuls.ledger.entity.utxoTransaction;

import io.nuls.core.constant.TransactionConstant;

/**
 * Created by facjas on 2017/11/17.
 */
public class UtxoDepositTransaction extends BaseUtxoCoinTransaction {
    public UtxoDepositTransaction() {
        this.type = TransactionConstant.TX_TYPE_DEPOSIT;
    }
}
