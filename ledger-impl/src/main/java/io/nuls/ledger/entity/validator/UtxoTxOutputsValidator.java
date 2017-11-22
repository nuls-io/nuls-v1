package io.nuls.ledger.entity.validator;

import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.AbstractCoinTransaction;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoOutput;

import java.util.List;

/**
 * Created by Niels on 2017/11/20.
 */
public class UtxoTxOutputsValidator implements NulsDataValidator<AbstractCoinTransaction<UtxoData>> {
    private static final int MAX_INPUT_COUNT = 200;
    private static final String ERROR_MESSAGE = "the output is too much!";
    @Override
    public ValidateResult validate(AbstractCoinTransaction<UtxoData> data) {
        UtxoData utxoData = data.getTxData();
        List<UtxoOutput> outputs = utxoData.getOutputs();
        if(null!=outputs&&outputs.size()>MAX_INPUT_COUNT){
            return ValidateResult.getFaildResult(ERROR_MESSAGE);
        }
        return ValidateResult.getSuccessResult();
    }
}
