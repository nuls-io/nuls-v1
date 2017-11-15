package io.nuls.consensus.entity;

import io.nuls.consensus.constant.ConsensusStatusEnum;

/**
 * Created by Niels on 2017/11/7.
 *
 */
public class ConsensusInfo {
    private ConsensusStatusEnum status = ConsensusStatusEnum.IN_CONSENSUS;
    private long startTime;
    private String bailorAddress;
    private String minerAddress;
    private int blockCount;
    private double rewardsNuls;
    private double marginAmount;
    private double weight;//%

    public ConsensusStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ConsensusStatusEnum status) {
        this.status = status;
    }

    public double getMarginAmount() {
        return marginAmount;
    }

    public void setMarginAmount(double marginAmount) {
        this.marginAmount = marginAmount;
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

    public String getBailorAddress() {
        return bailorAddress;
    }

    public void setBailorAddress(String bailorAddress) {
        this.bailorAddress = bailorAddress;
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public void setMinerAddress(String minerAddress) {
        this.minerAddress = minerAddress;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

    public double getRewardsNuls() {
        return rewardsNuls;
    }

    public void setRewardsNuls(double rewardsNuls) {
        this.rewardsNuls = rewardsNuls;
    }
}
