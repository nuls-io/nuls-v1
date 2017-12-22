package io.nuls.ledger.entity.validator;

import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class CoinDataValidator implements NulsDataValidator<AbstractCoinTransaction> {
    private static final CoinDataValidator INSTANCE = new CoinDataValidator();

    private CoinDataValidator() {
    }

    @Override
    public ValidateResult validate(AbstractCoinTransaction data) {
        return data.getCoinData().verify();
    }

    public static CoinDataValidator getInstance() {
        return INSTANCE;
    }
}
