package io.nuls.consensus.entity;

import io.nuls.consensus.constant.ConsensusStatusEnum;

/**
 * Created by Niels on 2017/11/7.
 *
 */
public class ConsensusInfo {
    private ConsensusStatusEnum status;
    private long startTime;
    private String entrustAddress;
    private String minerAddress;
    private int parkedCount;
    private double reward;
    private double deposit;
    private double weight;//%

    public ConsensusStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ConsensusStatusEnum status) {
        this.status = status;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public void setMinerAddress(String minerAddress) {
        this.minerAddress = minerAddress;
    }

    public int getParkedCount() {
        return parkedCount;
    }

    public void setParkedCount(int parkedCount) {
        this.parkedCount = parkedCount;
    }

    public String getEntrustAddress() {
        return entrustAddress;
    }

    public void setEntrustAddress(String entrustAddress) {
        this.entrustAddress = entrustAddress;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }
}
