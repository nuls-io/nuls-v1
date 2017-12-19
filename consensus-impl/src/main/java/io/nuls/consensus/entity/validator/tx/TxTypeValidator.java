package io.nuls.consensus.entity.validator.tx;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class TxTypeValidator implements NulsDataValidator<Transaction> {
    private static final String ERROR_MESSAGE = "The transaction type cannot null!";

    @Override
    public ValidateResult validate(Transaction data) {
        if (data == null) {
            return ValidateResult.getFaildResult(SeverityLevelEnum.NORMAL,"Data is null!");
        }
        if (data.getType() == 0) {
            return ValidateResult.getFaildResult(SeverityLevelEnum.NORMAL,ERROR_MESSAGE);
        }
        return ValidateResult.getSuccessResult();
    }
}
