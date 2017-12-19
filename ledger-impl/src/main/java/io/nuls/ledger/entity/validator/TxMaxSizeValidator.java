package io.nuls.ledger.entity.validator;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class TxMaxSizeValidator implements NulsDataValidator<Transaction> {
    public static final int MAX_STANDARD_TX_SIZE = 100000;
    private static final TxMaxSizeValidator INSTANCE = new TxMaxSizeValidator();

    private TxMaxSizeValidator() {
    }

    public static TxMaxSizeValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Transaction data) {
        if (data.size() > MAX_STANDARD_TX_SIZE) {
            return ValidateResult.getFailedResult(SeverityLevelEnum.NORMAL, ErrorCode.DATA_SIZE_ERROR);
        }
        return ValidateResult.getSuccessResult();
    }
}
