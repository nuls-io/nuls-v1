package io.nuls.consensus.entity.validator.tx;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * Created by Niels on 2017/11/20.
 */
public class TxSignValidator implements NulsDataValidator<Transaction> {
    @Override
    public ValidateResult validate(Transaction data) {
        //todo verify sign
        return null;
    }
}
