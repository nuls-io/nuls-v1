package io.nuls.consensus.entity.validator.tx;

import io.nuls.consensus.constant.PunishReasonEnum;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.service.intf.CoinDataProvider;

/**
 * @author Niels
 * @date 2017/12/28
 */
public class RedPunishValidator implements NulsDataValidator<RedPunishTransaction> {

    private static final RedPunishValidator INSTANCE = new RedPunishValidator();

    private CoinDataProvider coinDataProvider = NulsContext.getInstance().getService(CoinDataProvider.class);

    private RedPunishValidator() {
    }

    public static RedPunishValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(RedPunishTransaction data) {
        if (data.getTxData().getReasonCode() != PunishReasonEnum.DOUBLE_SPEND.getCode()) {
            return ValidateResult.getSuccessResult();
        }
        return  data.getTxData().verify();
    }
}
