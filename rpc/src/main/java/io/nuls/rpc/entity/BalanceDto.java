package io.nuls.rpc.entity;

import io.nuls.ledger.entity.Balance;

public class BalanceDto {

    private double balance;

    private double usable;

    private double locked;

    public BalanceDto(Balance balance) {
        this.balance = balance.getBalance().toDouble();
        this.usable = balance.getUsable().toDouble();
        this.locked = balance.getUsable().toDouble();
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getUsable() {
        return usable;
    }

    public void setUsable(double usable) {
        this.usable = usable;
    }

    public double getLocked() {
        return locked;
    }

    public void setLocked(double locked) {
        this.locked = locked;
    }
}
