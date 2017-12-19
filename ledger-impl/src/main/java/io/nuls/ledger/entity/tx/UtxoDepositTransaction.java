package io.nuls.ledger.entity.tx;

import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.validator.UtxoTxInputsValidator;
import io.nuls.ledger.entity.validator.UtxoTxOutputsValidator;

/**
 * @author facjas
 * @date 2017/11/17
 */
public class UtxoDepositTransaction extends LockNulsTransaction<UtxoData>  {
    public UtxoDepositTransaction() {
        this.setCanBeUnlocked(true);
        this.setUnlockHeight(0);
        this.setUnlockTime(0);
        this.registerValidator(UtxoTxInputsValidator.getInstance());
        this.registerValidator(UtxoTxOutputsValidator.getInstance());
    }

}
