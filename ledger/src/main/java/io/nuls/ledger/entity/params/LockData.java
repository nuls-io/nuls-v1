package io.nuls.ledger.entity.params;

import io.nuls.core.chain.entity.Na;

/**
 * @author Niels
 * @date 2017/12/21
 */
public class LockData {

    private String address;

    private Na na;

    private long unlockTime;

    private int unlockHeight;

    private boolean canBeUnlocked;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Na getNa() {
        return na;
    }

    public void setNa(Na na) {
        this.na = na;
    }

    public long getUnlockTime() {
        return unlockTime;
    }

    public void setUnlockTime(long unlockTime) {
        this.unlockTime = unlockTime;
    }

    public int getUnlockHeight() {
        return unlockHeight;
    }

    public void setUnlockHeight(int unlockHeight) {
        this.unlockHeight = unlockHeight;
    }

    public boolean isCanBeUnlocked() {
        return canBeUnlocked;
    }

    public void setCanBeUnlocked(boolean canBeUnlocked) {
        this.canBeUnlocked = canBeUnlocked;
    }
}
