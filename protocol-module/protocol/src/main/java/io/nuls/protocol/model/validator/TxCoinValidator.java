package io.nuls.protocol.model.validator;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;
import java.util.List;

/**
 * @author tag
 */
@Component
public class TxCoinValidator implements NulsDataValidator<Transaction> {
    @Override
    public ValidateResult validate(Transaction tx) throws NulsException {
        try {
            if(tx.getCoinData() == null){
                return ValidateResult.getSuccessResult();
            }
            List<Coin> toList = tx.getCoinData().getTo();
            if(toList == null || toList.size() == 0){
                return ValidateResult.getSuccessResult();
            }
            for (Coin coin:toList) {
                if(coin.getOwner().length == Address.ADDRESS_LENGTH && coin.getOwner()[2] == NulsContext.P2SH_ADDRESS_TYPE){
                    return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.COIN_OWNER_ERROR);
                }
            }
        } catch (Exception e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.DATA_ERROR);
        }
        return ValidateResult.getSuccessResult();
    }
}
