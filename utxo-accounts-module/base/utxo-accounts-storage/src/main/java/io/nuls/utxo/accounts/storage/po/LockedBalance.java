package io.nuls.utxo.accounts.storage.po;

import java.io.Serializable;

public class LockedBalance  implements Serializable {
    private long lockedTime=0;
    private long lockedBalance=0;

    public long getLockedTime() {
        return lockedTime;
    }

    public void setLockedTime(long lockedTime) {
        this.lockedTime = lockedTime;
    }

    public long getLockedBalance() {
        return lockedBalance;
    }

    public void setLockedBalance(long lockedBalance) {
        this.lockedBalance = lockedBalance;
    }

    public static int compareByLockedTime(LockedBalance b1, LockedBalance b2) {
        return (b1.getLockedTime()< b2.getLockedTime()) ? 1 : ((b1.getLockedTime() == b2.getLockedTime()) ? 0 : -1);
    }
}
