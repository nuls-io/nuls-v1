package io.nuls.consensus.entity;

import io.nuls.consensus.constant.ConsensusStatusEnum;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class ConsensusStatusInfo {
    private ConsensusStatusEnum status;
    private long startTime;
    private String agentAddress;
    private String delegatePeerAddress;
    private int parkedCount;
    private double accumulativeReward;
    private double deposit;
    private double weightOfRound;

    public ConsensusStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ConsensusStatusEnum status) {
        this.status = status;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public String getDelegatePeerAddress() {
        return delegatePeerAddress;
    }

    public void setDelegatePeerAddress(String delegatePeerAddress) {
        this.delegatePeerAddress = delegatePeerAddress;
    }

    public int getParkedCount() {
        return parkedCount;
    }

    public void setParkedCount(int parkedCount) {
        this.parkedCount = parkedCount;
    }

    public double getAccumulativeReward() {
        return accumulativeReward;
    }

    public void setAccumulativeReward(double accumulativeReward) {
        this.accumulativeReward = accumulativeReward;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public double getWeightOfRound() {
        return weightOfRound;
    }

    public void setWeightOfRound(double weightOfRound) {
        this.weightOfRound = weightOfRound;
    }
}
