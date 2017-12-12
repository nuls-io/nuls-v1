package io.nuls.ledger.entity;

import io.nuls.core.chain.intf.NulsCloneable;

import java.io.Serializable;

/**
 *
 * @author Niels
 * @date 2017/11/13
 *
 */
public class Balance implements Serializable,NulsCloneable{

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

    @Override
    public Object copy() {
        Balance obj = new Balance();
        obj.setBalance(balance);
        obj.setLocked(locked);
        obj.setUseable(this.useable);
        return obj;
    }
}
