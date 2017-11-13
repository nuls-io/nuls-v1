package io.nuls.consensus.entity;

import io.nuls.account.entity.Address;

/**
 * Created by Niels on 2017/11/7.
 * nuls.io
 */
//todo 定义共识相关的字段
public class ConsensusAccount  {

    private long startTime;

    private int startBlockHeight;

    private byte[] applyTxId;

    private double marginAmount;

    private Address bailor;

    private Address miner;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getStartBlockHeight() {
        return startBlockHeight;
    }

    public void setStartBlockHeight(int startBlockHeight) {
        this.startBlockHeight = startBlockHeight;
    }

    public byte[] getApplyTxId() {
        return applyTxId;
    }

    public void setApplyTxId(byte[] applyTxId) {
        this.applyTxId = applyTxId;
    }

    public double getMarginAmount() {
        return marginAmount;
    }

    public void setMarginAmount(double marginAmount) {
        this.marginAmount = marginAmount;
    }

    public Address getBailor() {
        return bailor;
    }

    public void setBailor(Address bailor) {
        this.bailor = bailor;
    }

    public Address getMiner() {
        return miner;
    }

    public void setMiner(Address miner) {
        miner = miner;
    }
}
