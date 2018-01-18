package io.nuls.consensus.entity.validator.tx;

import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/28
 */
public class YellowPunishValidator implements NulsDataValidator<YellowPunishTransaction> {

    private static final YellowPunishValidator INSTANCE = new YellowPunishValidator();

    private YellowPunishValidator() {
    }

    public static YellowPunishValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(YellowPunishTransaction data) {
        return data.getTxData().verify();
    }
}
