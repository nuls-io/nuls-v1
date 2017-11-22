package io.nuls.consensus.entity;

import io.nuls.account.entity.Address;

/**
 *
 * @author Niels
 * @date 2017/11/7
 *
 */
//todo 定义共识相关的字段
public class ConsensusAccount  {

    private long startTime;

    private int startBlockHeight;

    private byte[] applyTxId;

    private double deposit;

    public Address agent;

    private Address miner;

    private double credit;

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

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public Address getMiner() {
        return miner;
    }

    public Address getAgent() {
        return agent;
    }

    public void setAgent(Address agent) {
        this.agent = agent;
    }

    public void setMiner(Address miner) {
        miner = miner;
    }
}
