package io.nuls.consensus.poc.tx.validator;

import io.nuls.consensus.poc.protocol.tx.StopAgentTransaction;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.service.AgentStorageService;
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
public class StopAgentTxValidator implements NulsDataValidator<StopAgentTransaction> {

    @Autowired
    private AgentStorageService agentStorageService;

    @Override
    public ValidateResult validate(StopAgentTransaction data) {

        AgentPo agentPo = agentStorageService.get(data.getTxData().getCreateTxHash());
        if(null==agentPo||agentPo.getDelHeight()>0){
            return ValidateResult.getFailedResult(this.getClass().getName(),"The agent is deleted or never created!");
        }
        P2PKHScriptSig sig = new P2PKHScriptSig();
        try {
            sig.parse(data.getScriptSig());
        } catch (NulsException e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), e.getMessage());
        }
        if (!Arrays.equals(agentPo.getAgentAddress(), AddressTool.getAddress(sig.getPublicKey()))) {
            ValidateResult result = ValidateResult.getFailedResult(this.getClass().getName(), "The agent does not belong to this address.");
            result.setLevel(SeverityLevelEnum.FLAGRANT_FOUL);
            return result;
        }
        return ValidateResult.getSuccessResult();
    }
}
