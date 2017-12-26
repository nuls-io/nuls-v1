package io.nuls.ledger.entity.params;

import io.nuls.core.chain.entity.Na;

/**
 * @author Niels
 * @date 2017/12/26
 */
public class Coin {
    private Na na;

    private long unlockTime;

    private long unlockHeight;

    private boolean canBeUnlocked;

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

    public long getUnlockHeight() {
        return unlockHeight;
    }

    public void setUnlockHeight(long unlockHeight) {
        this.unlockHeight = unlockHeight;
    }

    public boolean isCanBeUnlocked() {
        return canBeUnlocked;
    }

    public void setCanBeUnlocked(boolean canBeUnlocked) {
        this.canBeUnlocked = canBeUnlocked;
    }
}
