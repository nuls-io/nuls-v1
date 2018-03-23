package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * author Facjas
 * date 2018/3/23.
 */
public class AgentCountValidator implements NulsDataValidator<RegisterAgentTransaction> {
    @Override
    public ValidateResult validate(RegisterAgentTransaction tx) {
        ValidateResult result = ValidateResult.getSuccessResult();
        Agent agent = tx.getTxData().getExtend();
        String agentName = agent.getAgentName();
        String packingAddress = agent.getPackingAddress();

        //todo an address can only create one agent,and a packing address can only pack for one agent

        return result;
    }
}
