package io.nuls.rpc.entity;

import io.nuls.ledger.entity.Balance;

public class BalanceDto {

    private String balance;

    private String usable;

    private String locked;

    public BalanceDto(Balance balance) {
        this.balance = balance.getBalance().toDouble() + "";
        this.usable = balance.getUsable().toDouble() + "";
        this.locked = balance.getLocked().toDouble() + "";
    }


    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getUsable() {
        return usable;
    }

    public void setUsable(String usable) {
        this.usable = usable;
    }

    public String getLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }
}
