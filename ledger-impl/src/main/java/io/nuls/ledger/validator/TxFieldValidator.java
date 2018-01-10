package io.nuls.ledger.validator;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class TxFieldValidator implements NulsDataValidator<Transaction> {
    private static final TxFieldValidator INSTANCE = new TxFieldValidator();

    private TxFieldValidator() {
    }

    public static TxFieldValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Transaction data) {
        boolean result = true;
        do {
            if (data == null) {
                result = false;
                break;
            }
            if (data.getHash() == null || data.getHash().getDigestLength() == 0) {
                result = false;
                break;
            }
            if (data.getSign() == null || data.getSign().getSignLength() == 0) {
                result = false;
                break;
            }
            if (data.getType() == 0) {
                result = false;
                break;
            }
            if (data.getTime() == 0) {
                result = false;
                break;
            }
        } while (false);
        if (!result) {
            return ValidateResult.getFailedResult( ErrorCode.DATA_FIELD_CHECK_ERROR);
        }
        return ValidateResult.getSuccessResult();
    }
}