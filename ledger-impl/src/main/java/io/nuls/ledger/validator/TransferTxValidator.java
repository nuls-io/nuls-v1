package io.nuls.ledger.validator;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoInput;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.tx.TransferTransaction;

public class TransferTxValidator implements NulsDataValidator<TransferTransaction> {

    private static TransferTxValidator instance = new TransferTxValidator();

    private TransferTxValidator() {

    }

    public static TransferTxValidator getInstance() {
        return instance;
    }

    @Override
    public ValidateResult validate(TransferTransaction tx) {
        UtxoData data = (UtxoData) tx.getCoinData();

        long inTotal = 0;
        for (UtxoInput input : data.getInputs()) {
            inTotal += input.getFrom().getValue();
        }

        long outTotal = 0;
        for (UtxoOutput output : data.getOutputs()) {
            outTotal += output.getValue();
        }

        if (inTotal != tx.getFee().getValue() + outTotal) {
            return ValidateResult.getFailedResult(ErrorCode.INVALID_AMOUNT);
        }
        return ValidateResult.getSuccessResult();
    }
}
