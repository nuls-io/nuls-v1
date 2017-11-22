package io.nuls.ledger.entity.utxoTransaction;

import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.ledger.entity.AbstractCoinTransaction;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.validator.UtxoTxInputsValidator;
import io.nuls.ledger.entity.validator.UtxoTxOutputsValidator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/14.
 */
public class UtxoSmallChangeTransaction extends AbstractCoinTransaction<UtxoData> {

    public UtxoSmallChangeTransaction() {
        this.type = TransactionConstant.TX_TYPE_SMALL_CHANGE;
        this.registerValidator(new UtxoTxInputsValidator());
        this.registerValidator(new UtxoTxOutputsValidator());
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {

    }


}
