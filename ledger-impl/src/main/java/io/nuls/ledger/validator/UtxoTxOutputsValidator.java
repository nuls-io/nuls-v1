package io.nuls.ledger.validator;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;

import java.util.List;

/**
 *
 * @author Niels
 * @date 2017/11/20
 */
public class UtxoTxOutputsValidator implements NulsDataValidator<AbstractCoinTransaction<UtxoData>> {
    private static final int MAX_INPUT_COUNT = 200;
    private static final String ERROR_MESSAGE = "the output is too much!";
    private static final UtxoTxOutputsValidator INSTANCE = new UtxoTxOutputsValidator();
    private UtxoTxOutputsValidator(){}
    public static UtxoTxOutputsValidator getInstance(){
        return INSTANCE;
    }
    @Override
    public ValidateResult validate(AbstractCoinTransaction<UtxoData> data) {
        UtxoData utxoData = (UtxoData) data.getTxData();
        List<UtxoOutput> outputs = utxoData.getOutputs();
        if(null!=outputs&&outputs.size()>MAX_INPUT_COUNT){
            return ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        return ValidateResult.getSuccessResult();
    }
}
