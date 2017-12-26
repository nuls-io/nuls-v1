package io.nuls.ledger.entity;

import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.intf.NulsCloneable;

import java.io.Serializable;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class Balance implements Serializable, NulsCloneable {

    private Na balance;

    private Na locked;

    private Na useable;

    public Balance() {

    }

    public Balance(Na useable, Na locked) {
        this.useable = useable;
        this.locked = locked;
        this.balance = locked.add(useable);
    }

    public Na getBalance() {
        return balance;
    }

    public void setBalance(Na balance) {
        this.balance = balance;
    }

    public Na getLocked() {
        return locked;
    }

    public void setLocked(Na locked) {
        this.locked = locked;
    }

    public Na getUseable() {
        return useable;
    }

    public void setUseable(Na useable) {
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
