/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
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
