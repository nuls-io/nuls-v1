package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

import java.util.List;

/**
 * date 2018/3/23.
 * @author Facjas
 */
public class AgentCountValidator implements NulsDataValidator<RegisterAgentTransaction> {

    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();

    @Override
    public ValidateResult validate(RegisterAgentTransaction tx) {
        ValidateResult result = ValidateResult.getSuccessResult();
        Agent agent = tx.getTxData().getExtend();
        String agentName = agent.getAgentName();
        List<Consensus<Agent>> caList = consensusCacheManager.getCachedAgentList();
        if (caList != null) {
            for (Consensus<Agent> ca : caList) {
                if (ca.getAddress().equals(tx.getTxData().getAddress())) {
                    return ValidateResult.getFailedResult("An address can only create one agent");
                }
                if (ca.getAddress().equals(ca.getExtend().getPackingAddress())) {
                    return ValidateResult.getFailedResult("The address can only create one agent");
                }
                if (ca.getExtend().getPackingAddress().equals(ca.getAddress())) {
                    return ValidateResult.getFailedResult("The packingAddress is an agentAddress");
                }
                if (ca.getExtend().getPackingAddress().equals(ca.getExtend().getPackingAddress())) {
                    return ValidateResult.getFailedResult("The packingAddress is busy!");
                }
                if(agentName.equals(ca.getExtend().getAgentName())){
                    return ValidateResult.getFailedResult("AgentName repetition!");
                }
            }
        }
        return result;
    }
}
