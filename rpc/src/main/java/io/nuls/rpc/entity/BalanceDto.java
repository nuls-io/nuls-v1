package io.nuls.rpc.entity;

import io.nuls.ledger.entity.Balance;

public class BalanceDto {

    private Double balance;

    private Double usable;

    private Double locked;

    public BalanceDto(Balance balance) {
        this.balance = balance.getBalance().toDouble();
        this.usable = balance.getUsable().toDouble();
        this.locked = balance.getLocked().toDouble();
    }


    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getUsable() {
        return usable;
    }

    public void setUsable(Double usable) {
        this.usable = usable;
    }

    public Double getLocked() {
        return locked;
    }

    public void setLocked(Double locked) {
        this.locked = locked;
    }
}
