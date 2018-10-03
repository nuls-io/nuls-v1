package io.nuls.contract.entity.tx.validator;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.util.ContractUtil;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.util.List;

/**
 * @author tag
 */
@Component
public class ContractAcceptTransferredTxValidator implements NulsDataValidator<Transaction> {
    @Override
    public ValidateResult validate(Transaction tx) throws NulsException {
        if(tx.getCoinData() == null){
            return ValidateResult.getSuccessResult();
        }
        List<Coin> toList = tx.getCoinData().getTo();
        if(toList == null || toList.size() == 0){
            return ValidateResult.getSuccessResult();
        }
        int type = tx.getType();
        for (Coin coin : toList) {
            if(ContractUtil.isLegalContractAddress(coin.getOwner())) {
                if(type != NulsConstant.TX_TYPE_COINBASE && type != ContractConstant.TX_TYPE_CALL_CONTRACT) {
                    Log.error("contract data error: The contract does not accept transfers of this type[{}] of transaction.", type);
                    return ValidateResult.getFailedResult(this.getClass().getSimpleName(), TransactionErrorCode.TX_DATA_VALIDATION_ERROR);
                }
            }
        }
        return ValidateResult.getSuccessResult();
    }
}
