package io.nuls.consensus.entity;

import io.nuls.consensus.constant.POCConsensusConstant;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class RegisterAgentTransaction extends AbstractConsensusTransaction {

    private String address;
    private double deposit;
    private String delegateAddress;
    private double commissionRate;
    private String introduction;

    public RegisterAgentTransaction( ) {
        super(POCConsensusConstant.TX_TYPE_REGISTER_AGENT);
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public String getDelegateAddress() {
        return delegateAddress;
    }

    public void setDelegateAddress(String delegateAddress) {
        this.delegateAddress = delegateAddress;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
