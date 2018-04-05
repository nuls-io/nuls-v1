package io.nuls.ledger.validator;

import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.tx.TransferTransaction;

public class TransferTxValidator implements NulsDataValidator<TransferTransaction> {
    @Override
    public ValidateResult validate(TransferTransaction tx) {

        return null;
    }
}
