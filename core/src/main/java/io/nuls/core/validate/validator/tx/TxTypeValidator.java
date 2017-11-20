package io.nuls.core.validate.validator.tx;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * Created by Niels on 2017/11/17.
 */
public class TxTypeValidator implements NulsDataValidator<Transaction> {
    private static final String ERROR_MESSAGE = "The transaction type cannot null!";

    @Override
    public ValidateResult validate(Transaction data) {
        if (data == null) {
            return new ValidateResult(false, "Data is null!");
        }
        if (data.getType()==0) {
            return ValidateResult.getFaildResult(ERROR_MESSAGE);
        }
        return ValidateResult.getSuccessResult();
    }
}
