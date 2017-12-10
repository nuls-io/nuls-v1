package io.nuls.ledger.entity.tx;

import io.nuls.ledger.entity.tx.LockCoinTransaction;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.validator.UtxoTxInputsValidator;
import io.nuls.ledger.entity.validator.UtxoTxOutputsValidator;

/**
 * Created by Niels on 2017/11/14.
 */
public class UtxoLockTransaction extends LockCoinTransaction<UtxoData> {
    public UtxoLockTransaction(){
        this.setCanBeUnlocked(false);
        this.registerValidator(new UtxoTxInputsValidator());
        this.registerValidator(new UtxoTxOutputsValidator());
    }
}
