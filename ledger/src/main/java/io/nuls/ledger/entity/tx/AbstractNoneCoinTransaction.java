package io.nuls.ledger.entity.tx;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.ledger.entity.validator.CoinDataValidator;
import io.nuls.ledger.entity.validator.CoinTransactionValidatorManager;

import java.util.List;

/**
 * author Facjas
 * date 2018/4/3.
 */
public abstract class AbstractNoneCoinTransaction <T extends BaseNulsData> extends Transaction<T> {
    public AbstractNoneCoinTransaction(int type) {
        super(type);
    }
}
