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
public class TxRemarkValidator implements NulsDataValidator<Transaction> {
    public final static int MAX_REMARK_LEN = 256;
    private static final TxRemarkValidator INSTANCE = new TxRemarkValidator();

    private TxRemarkValidator() {
    }

    public static TxRemarkValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Transaction data) {
        byte[] remark = data.getRemark();
        if (remark != null && remark.length > MAX_REMARK_LEN) {
            return ValidateResult.getFailedResult(SeverityLevelEnum.NORMAL, ErrorCode.DATA_SIZE_ERROR);
        }
        return ValidateResult.getSuccessResult();
    }
}
