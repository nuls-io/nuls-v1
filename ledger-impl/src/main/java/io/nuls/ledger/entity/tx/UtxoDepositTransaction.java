package io.nuls.ledger.entity.tx;

import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.ledger.entity.tx.LockCoinTransaction;
import io.nuls.ledger.entity.validator.UtxoTxInputsValidator;
import io.nuls.ledger.entity.validator.UtxoTxOutputsValidator;

import java.io.IOException;

/**
 * Created by facjas on 2017/11/17.
 */
public class UtxoDepositTransaction extends LockCoinTransaction {
    public UtxoDepositTransaction() {
        this.setCanBeUnlocked(true);
        this.registerValidator(new UtxoTxInputsValidator());
        this.registerValidator(new UtxoTxOutputsValidator());
    }

    @Override
    public int size() {
        //todo
        return 0;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        //todo

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {

        //todo
    }
}
