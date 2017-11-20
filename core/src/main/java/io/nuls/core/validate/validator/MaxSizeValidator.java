package io.nuls.core.validate.validator;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * Created by Niels on 2017/11/17.
 */
public class MaxSizeValidator implements NulsDataValidator<Transaction> {
    private static final int MAX_SIZE = 2 << 21;//2M
    private static final String ERROR_MESSAGE = "The transaction is too big!";

    @Override
    public ValidateResult validate(Transaction data) {
        if (data == null) {
            return new ValidateResult(false, "Data is null!");
        }
        int length = data.size();
        if(length>=MAX_SIZE){
            return ValidateResult.getFaildResult(ERROR_MESSAGE);
        }
        return ValidateResult.getSuccessResult();
    }
}
