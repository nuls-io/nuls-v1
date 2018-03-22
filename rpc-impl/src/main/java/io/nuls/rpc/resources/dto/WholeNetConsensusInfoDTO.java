package io.nuls.rpc.resources.dto;

/**
 * @author: Niels Wang
 * @date: 2018/3/20
 */
public class WholeNetConsensusInfoDTO {

    private int agentCount;
    private long totalDeposit;
    private long rewardOfDay;
    private int consensusAccountNumber;

    public int getConsensusAccountNumber() {
        return consensusAccountNumber;
    }

    public void setConsensusAccountNumber(int consensusAccountNumber) {
        this.consensusAccountNumber = consensusAccountNumber;
    }

    public int getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

    public long getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(long totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public long getRewardOfDay() {
        return rewardOfDay;
    }

    public void setRewardOfDay(long rewardOfDay) {
        this.rewardOfDay = rewardOfDay;
    }
}
