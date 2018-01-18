package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AgentDepositValidator implements NulsDataValidator<RegisterAgentTransaction> {
    @Override
    public ValidateResult validate(RegisterAgentTransaction tx) {
        ValidateResult result = ValidateResult.getSuccessResult();
        Agent agent = tx.getTxData().getExtend();
        if (null == agent) {
            return ValidateResult.getFailedResult(ErrorCode.NULL_PARAMETER);
        }
        if (!agent.getDeposit().isGreaterThan(PocConsensusConstant.AGENT_DEPOSIT_LOWER_LIMIT)) {
            result = ValidateResult.getFailedResult(ErrorCode.DEPOSIT_NOT_ENOUGH);
        }
        return result;
    }
}
