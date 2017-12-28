package io.nuls.ledger.validator;

import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/20
 */
public class TxSignValidator implements NulsDataValidator<Transaction> {
    private static final TxSignValidator INSTANCE = new TxSignValidator();

    private TxSignValidator() {
    }

    public static TxSignValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Transaction data) {
        //todo verify sign
        return null;
    }
}