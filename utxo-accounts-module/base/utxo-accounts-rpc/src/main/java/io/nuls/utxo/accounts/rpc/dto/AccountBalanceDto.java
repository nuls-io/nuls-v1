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
package io.nuls.utxo.accounts.rpc.dto;

import io.nuls.utxo.accounts.storage.po.LockedBalance;

import java.util.ArrayList;
import java.util.List;

public class AccountBalanceDto {
    private String address;
    private String synBlockHeight;
    private String netBlockHeight;
    private String nuls;
    private String locked;
    private String permanentLocked;
    private String timeLocked;
    private String heightLocked;
    private String contractIn;
    private String contractOut;
    private List<LockedBalance> lockedTimeList=new ArrayList<>();
    private List<LockedBalance> lockedHeightList=new ArrayList<>();


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNuls() {
        return nuls;
    }

    public void setNuls(String nuls) {
        this.nuls = nuls;
    }

    public String getLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }

    public String getSynBlockHeight() {
        return synBlockHeight;
    }

    public void setSynBlockHeight(String synBlockHeight) {
        this.synBlockHeight = synBlockHeight;
    }

    public String getNetBlockHeight() {
        return netBlockHeight;
    }

    public void setNetBlockHeight(String netBlockHeight) {
        this.netBlockHeight = netBlockHeight;
    }

    public String getPermanentLocked() {
        return permanentLocked;
    }

    public void setPermanentLocked(String permanentLocked) {
        this.permanentLocked = permanentLocked;
    }

    public String getTimeLocked() {
        return timeLocked;
    }

    public void setTimeLocked(String timeLocked) {
        this.timeLocked = timeLocked;
    }

    public String getHeightLocked() {
        return heightLocked;
    }

    public void setHeightLocked(String heightLocked) {
        this.heightLocked = heightLocked;
    }

    public String getContractIn() {
        return contractIn;
    }

    public void setContractIn(String contractIn) {
        this.contractIn = contractIn;
    }

    public String getContractOut() {
        return contractOut;
    }

    public void setContractOut(String contractOut) {
        this.contractOut = contractOut;
    }

    public List<LockedBalance> getLockedTimeList() {
        return lockedTimeList;
    }

    public void setLockedTimeList(List<LockedBalance> lockedTimeList) {
        this.lockedTimeList = lockedTimeList;
    }

    public List<LockedBalance> getLockedHeightList() {
        return lockedHeightList;
    }

    public void setLockedHeightList(List<LockedBalance> lockedHeightList) {
        this.lockedHeightList = lockedHeightList;
    }
}
