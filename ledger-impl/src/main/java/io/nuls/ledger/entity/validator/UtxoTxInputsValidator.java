package io.nuls.ledger.entity.validator;

import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.AbstractCoinTransaction;

/**
 * Created by Niels on 2017/11/20.
 */
public class UtxoTxInputsValidator implements NulsDataValidator<AbstractCoinTransaction> {
    @Override
    public ValidateResult validate(AbstractCoinTransaction data) {
        //todo 是否输入为空,引用了不可用的输出,输出个数,对父交易的引用
        return null;
    }
}
