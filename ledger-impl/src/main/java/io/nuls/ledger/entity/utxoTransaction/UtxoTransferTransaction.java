package io.nuls.ledger.entity.utxoTransaction;

import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.TransferTransaction;
import io.nuls.ledger.entity.UtxoData;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/14.
 */
public class UtxoTransferTransaction extends TransferTransaction<UtxoData> {

    public UtxoTransferTransaction() {
        this.type = TransactionConstant.TX_TYPE_TRANSFER;
    }

}
