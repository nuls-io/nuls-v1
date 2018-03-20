package io.nuls.rpc.resources.dto;

/**
 * @author: Niels Wang
 * @date: 2018/3/20
 */
public class ConsensusInfoDTO {

    private int agentCount;
    private long totalDeposit;
    private long reward;
    private int delegateAgentCount;
    private long usableBalance;
    private long rewardOfDay;

    public long getRewardOfDay() {
        return rewardOfDay;
    }

    public void setRewardOfDay(long rewardOfDay) {
        this.rewardOfDay = rewardOfDay;
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

    public long getReward() {
        return reward;
    }

    public void setReward(long reward) {
        this.reward = reward;
    }

    public int getDelegateAgentCount() {
        return delegateAgentCount;
    }

    public void setDelegateAgentCount(int delegateAgentCount) {
        this.delegateAgentCount = delegateAgentCount;
    }

    public long getUsableBalance() {
        return usableBalance;
    }

    public void setUsableBalance(long usableBalance) {
        this.usableBalance = usableBalance;
    }
}
