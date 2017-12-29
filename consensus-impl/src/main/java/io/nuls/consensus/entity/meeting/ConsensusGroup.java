package io.nuls.consensus.entity.meeting;

import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.core.chain.entity.Na;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/29
 */
public class ConsensusGroup {
    private Na allDepositNa;
    private Consensus<Agent> agentConsensus;
    private List<Consensus<Delegate>> delegateList;

    public Consensus<Agent> getAgentConsensus() {
        return agentConsensus;
    }

    public void setAgentConsensus(Consensus<Agent> agentConsensus) {
        this.agentConsensus = agentConsensus;
    }

    public List<Consensus<Delegate>> getDelegateList() {
        return delegateList;
    }

    public void setDelegateList(List<Consensus<Delegate>> delegateList) {
        this.delegateList = delegateList;
    }
}
