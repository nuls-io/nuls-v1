package io.nuls.ledger.entity.validator;

import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.CoinTransaction;

/**
 * Created by Niels on 2017/11/20.
 */
public class UtxoTxInputsValidator implements NulsDataValidator<CoinTransaction> {
    @Override
    public ValidateResult validate(CoinTransaction data) {
        //todo 是否输入为空,引用了不可用的输出,输出个数,对父交易的引用
        return null;
    }
}
