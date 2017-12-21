package io.nuls.ledger.entity.tx;

import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.validator.UtxoTxInputsValidator;
import io.nuls.ledger.entity.validator.UtxoTxOutputsValidator;

/**
 * Created by Niels on 2017/11/14.
 */
public class UtxoLockTransaction extends LockNulsTransaction  {
    public UtxoLockTransaction(){
        this.registerValidator(UtxoTxInputsValidator.getInstance());
        this.registerValidator(UtxoTxOutputsValidator.getInstance());
    }
}
