package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AgentDepositValidator implements NulsDataValidator<RegisterAgentTransaction> {
    @Override
    public ValidateResult validate(RegisterAgentTransaction data) {
        ValidateResult result = ValidateResult.getSuccessResult();

//todo         if(!tx.getNa().isGreaterThan(PocConsensusConstant.AGENT_DEPOSIT_LOWER_LIMIT)){
//            result = ValidateResult.getFailedResult(ErrorCode.DEPOSIT_NOT_ENOUGH);
//        }
        return result;
    }
}
