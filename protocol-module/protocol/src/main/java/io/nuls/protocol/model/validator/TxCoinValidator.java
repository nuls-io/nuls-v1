package io.nuls.protocol.model.validator;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.map.MapUtil;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.script.SignatureUtil;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tag
 */
@Component
public class TxCoinValidator implements NulsDataValidator<Transaction> {
    @Override
    public ValidateResult validate(Transaction tx) throws NulsException {
        try {
            if (tx.getCoinData() == null) {
                return ValidateResult.getSuccessResult();
            }
            List<Coin> toList = tx.getCoinData().getTo();
            if (toList == null || toList.size() == 0) {
                return ValidateResult.getSuccessResult();
            }

            Set<String> fromAddressSet = null;
            byte[] owner;
            String address;
            Map<String, Integer> addressSet = MapUtil.createHashMap(toList.size());
            for (Coin coin : toList) {
                owner = coin.getOwner();
                if (owner.length == Address.ADDRESS_LENGTH && owner[2] == NulsContext.P2SH_ADDRESS_TYPE) {
                    return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.COIN_OWNER_ERROR);
                }
                if(tx.isSystemTx() && tx.getType() != ContractConstant.TX_TYPE_CONTRACT_TRANSFER) {
                    continue;
                }
                address = AddressTool.getStringAddressByBytes(coin.getAddress());
                if(fromAddressSet == null) {
                    fromAddressSet = SignatureUtil.getAddressFromTX(tx);
                }
                if(fromAddressSet != null && fromAddressSet.contains(address)) {
                    continue;
                }
                Integer count = addressSet.get(address);
                if(count == null) {
                    addressSet.put(address, count = 1);
                } else {
                    addressSet.put(address, ++count);
                }
                if(count > 2) {
                    return ValidateResult.getFailedResult(this.getClass().getName(), TransactionErrorCode.INVALID_AMOUNT);
                }

            }
        } catch (Exception e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.DATA_ERROR);
        }
        return ValidateResult.getSuccessResult();
    }
}
