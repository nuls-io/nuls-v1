package io.nuls.consensus.poc.tx.validator;

import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.tx.CancelDepositTransaction;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.consensus.poc.storage.service.DepositStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.SeverityLevelEnum;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.util.Arrays;

/**
 * @author Niels
 * @date 2018/5/17
 */
@Component
public class CancelDepositTxValidator implements NulsDataValidator<CancelDepositTransaction> {

    @Autowired
    private DepositStorageService depositStorageService;

    @Override
    public ValidateResult validate(CancelDepositTransaction data) {
        DepositPo depositPo = depositStorageService.get(data.getTxData().getJoinTxHash());
        if(null==depositPo||depositPo.getDelHeight()>0){
            return ValidateResult.getFailedResult(this.getClass().getName(),"The deposit is deleted or never created!");
        }
        P2PKHScriptSig sig = new P2PKHScriptSig();
        try {
            sig.parse(data.getScriptSig());
        } catch (NulsException e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), e.getMessage());
        }
        if (!Arrays.equals(depositPo.getAddress(), AddressTool.getAddress(sig.getPublicKey()))) {
            ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), "The deposit does not belong to this address.");
            result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
            return result;
        }
        return ValidateResult.getSuccessResult();
    }
}
