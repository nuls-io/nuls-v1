package io.nuls.ledger.validator;

import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

public class LockTxValidator implements NulsDataValidator<LockNulsTransaction> {

    @Override
    public ValidateResult validate(LockNulsTransaction data) {
        return null;
    }
}
