package io.nuls.consensus.entity.tx;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Agent;
import io.nuls.consensus.entity.validator.consensus.AccountCreditValidator;
import io.nuls.consensus.entity.validator.consensus.AgentDepositLimitValidator;
import io.nuls.consensus.tx.AbstractConsensusTransaction;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RegisterAgentTransaction extends AbstractConsensusTransaction<Agent> {

    public RegisterAgentTransaction() {
        super(PocConsensusConstant.TX_TYPE_REGISTER_AGENT);
        this.registerValidator(new AccountCreditValidator());
        this.registerValidator(new AgentDepositLimitValidator());


    }


    @Override
    protected Agent parseBody(NulsByteBuffer byteBuffer) {
        Agent agent = new Agent();
        agent.parse(byteBuffer);
        return agent;
    }
}
