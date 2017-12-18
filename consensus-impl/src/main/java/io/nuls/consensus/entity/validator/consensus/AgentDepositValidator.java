package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.tx.LockNulsTransaction;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AgentDepositValidator implements NulsDataValidator<RegisterAgentTransaction> {
    @Override
    public ValidateResult validate(RegisterAgentTransaction data) {
        ValidateResult result = ValidateResult.getSuccessResult();
        LockNulsTransaction tx = data.getLockNulsTransaction();
        if(null==tx){
            result = ValidateResult.getFaildResult(ErrorCode.DEPOSIT_ERROR);
        }
        else if(!tx.getNa().isGreaterThan(PocConsensusConstant.AGENT_DEPOSIT_LOWER_LIMIT)){
            result = ValidateResult.getFaildResult(ErrorCode.DEPOSIT_NOT_ENOUGH);
        }
        tx.verify();
        return result;
    }
}
