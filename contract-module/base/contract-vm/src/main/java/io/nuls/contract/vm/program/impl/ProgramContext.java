package io.nuls.contract.vm.program.impl;

import io.nuls.contract.vm.ObjectRef;

public class ProgramContext {

    private ObjectRef address;

    private ObjectRef sender;

    //private ObjectRef balance;

    private long gasPrice;

    private long gas;

    //private long gasLimit;

    private ObjectRef value;

    private long number;

    //private long difficulty;

    //private ObjectRef data;

    private boolean estimateGas;

    public ObjectRef getAddress() {
        return address;
    }

    public void setAddress(ObjectRef address) {
        this.address = address;
    }

    public ObjectRef getSender() {
        return sender;
    }

    public void setSender(ObjectRef sender) {
        this.sender = sender;
    }

    public long getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(long gasPrice) {
        this.gasPrice = gasPrice;
    }

    public long getGas() {
        return gas;
    }

    public void setGas(long gas) {
        this.gas = gas;
    }

    public ObjectRef getValue() {
        return value;
    }

    public void setValue(ObjectRef value) {
        this.value = value;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public boolean isEstimateGas() {
        return estimateGas;
    }

    public void setEstimateGas(boolean estimateGas) {
        this.estimateGas = estimateGas;
    }

}
