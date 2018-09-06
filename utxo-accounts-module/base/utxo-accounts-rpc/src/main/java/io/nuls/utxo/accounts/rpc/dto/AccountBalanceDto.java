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
