package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/14.
 */
public class TransferTransaction extends UtxoCoinTransaction {

    public TransferTransaction() {
        this.type = TransactionConstant.TX_TYPE_TRANSFER;
    }

}
