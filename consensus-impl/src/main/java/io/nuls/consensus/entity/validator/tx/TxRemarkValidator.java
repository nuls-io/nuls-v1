package io.nuls.consensus.entity.validator.tx;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * Created by Niels on 2017/11/20.
 */
public class TxRemarkValidator implements NulsDataValidator<Transaction> {
    /**
     * default : 100 byte
     */
    private static final int MAX_SIZE = 100;

    private static final String ERROR_MESSAGE = "The remark of The transaction is too big!";

    @Override
    public ValidateResult validate(Transaction data) {
        if (data == null) {
            return ValidateResult.getFaildResult( "Data is null!");
        }
        if(null==data.getRemark()){
            return ValidateResult.getSuccessResult();
        }
        int length = data.getRemark().length;
        if (length >= MAX_SIZE) {
            return ValidateResult.getFaildResult(ERROR_MESSAGE);
        }
        return ValidateResult.getSuccessResult();
    }
}
