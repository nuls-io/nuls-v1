package io.nuls.ledger.entity;

import io.nuls.core.constant.TransactionConstant;

/**
 * Created by Niels on 2017/11/14.
 */
public class TransferTransaction extends NulsTransaction {

    public TransferTransaction() {
        this.type = TransactionConstant.TX_TYPE_TRANSFER;
    }
}
