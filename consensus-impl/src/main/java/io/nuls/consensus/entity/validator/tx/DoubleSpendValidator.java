package io.nuls.consensus.entity.validator.tx;

import io.nuls.consensus.constant.PunishReasonEnum;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/28
 */
public class DoubleSpendValidator implements NulsDataValidator<RedPunishTransaction> {

    private static final DoubleSpendValidator INSTANCE = new DoubleSpendValidator();

    private DoubleSpendValidator() {
    }

    public static DoubleSpendValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(RedPunishTransaction data) {
        if (data.getTxData().getReasonCode() != PunishReasonEnum.DOUBLE_SPEND.getCode()) {
            return ValidateResult.getSuccessResult();
        }
        // todo auto-generated method stub(niels)
        return null;
    }
}
