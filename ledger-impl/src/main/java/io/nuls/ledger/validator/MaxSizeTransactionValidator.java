package io.nuls.ledger.validator;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.chain.validator.DataValidatorChain;
import io.nuls.core.chain.validator.NulsDataValidator;
import io.nuls.core.chain.validator.ValidateResult;

/**
 * Created by Niels on 2017/11/17.
 */
public class MaxSizeTransactionValidator implements NulsDataValidator<Transaction> {
    private static final int MAX_SIZE = 2 << 21;

    @Override
    public ValidateResult validate(Transaction data, DataValidatorChain dataValidatorChain) {
        if (data == null) {
            return new ValidateResult(false, "Data is null!");
        }
        data.verify();
        return dataValidatorChain.doValidate(data);
    }
}
