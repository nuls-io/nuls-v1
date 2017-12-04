package io.nuls.consensus.entity;

import io.nuls.account.entity.Address;

/**
 *
 * @author Niels
 * @date 2017/11/7
 *
 */
public class ConsensusAccount  {

    private long startTime;

    private int startBlockHeight;

    private byte[] applyTxId;

    private double deposit;

    public Address delegate;

    private Address delegatePeer;

    private Address address;

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

    public Address getDelegate() {
        return delegate;
    }

    public void setDelegate(Address delegate) {
        this.delegate = delegate;
    }

    public Address getDelegatePeer() {
        return delegatePeer;
    }

    public void setDelegatePeer(Address delegatePeer) {
        this.delegatePeer = delegatePeer;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
