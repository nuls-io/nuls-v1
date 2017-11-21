package io.nuls.ledger.entity;

import java.io.Serializable;

/**
 * Created by Niels on 2017/11/13.
 *
 */
public class Balance implements Serializable{

    private double balance;

    private double locked;

    private double useable;

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getLocked() {
        return locked;
    }

    public void setLocked(double locked) {
        this.locked = locked;
    }

    public double getUseable() {
        return useable;
    }

    public void setUseable(double useable) {
        this.useable = useable;
    }
}
