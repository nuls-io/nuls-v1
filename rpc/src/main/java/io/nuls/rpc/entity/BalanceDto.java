package io.nuls.rpc.entity;

import io.nuls.ledger.entity.Balance;

public class BalanceDto {

    private long balance;

    private long usable;

    private long locked;

    public BalanceDto(Balance balance) {
        if (balance == null) {
            this.balance = 0;
            this.usable = 0;
            this.locked = 0;
        } else {
            this.balance = balance.getBalance().getValue();
            this.usable = balance.getUsable().getValue();
            this.locked = balance.getLocked().getValue();
        }
    }


    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getUsable() {
        return usable;
    }

    public void setUsable(long usable) {
        this.usable = usable;
    }

    public long getLocked() {
        return locked;
    }

    public void setLocked(long locked) {
        this.locked = locked;
    }
}
