package io.nuls.consensus.poc.rpc.model;

/**
 * @author Niels
 * @date 2018/5/16
 */
public class AccountConsensusInfoDTO {
    private int agentCount;
    private long totalDeposit;
    private int joinAgentCount;
    private long usableBalance;
    private long reward;
    private long rewardOfDay;
    private String agentHash;

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

    public int getJoinAgentCount() {
        return joinAgentCount;
    }

    public void setJoinAgentCount(int joinAgentCount) {
        this.joinAgentCount = joinAgentCount;
    }

    public long getUsableBalance() {
        return usableBalance;
    }

    public void setUsableBalance(long usableBalance) {
        this.usableBalance = usableBalance;
    }

    public long getReward() {
        return reward;
    }

    public void setReward(long reward) {
        this.reward = reward;
    }

    public long getRewardOfDay() {
        return rewardOfDay;
    }

    public void setRewardOfDay(long rewardOfDay) {
        this.rewardOfDay = rewardOfDay;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public String getAgentHash() {
        return agentHash;
    }
}
