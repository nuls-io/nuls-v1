package io.nuls.ledger.rpc.model;

import io.nuls.core.tools.calc.DoubleUtils;

/**
 * @author: Niels Wang
 * @date: 2018/10/11
 */
public class Holder implements Comparable {

    private String address;

    private double totalNuls;

    private double lockedNuls;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getTotalNuls() {
        return totalNuls;
    }

    public void setTotalNuls(double totalNuls) {
        this.totalNuls = totalNuls;
    }

    public double getLockedNuls() {
        return lockedNuls;
    }

    public void setLockedNuls(double lockedNuls) {
        this.lockedNuls = lockedNuls;
    }

    public void addTotal(double value) {
        totalNuls = DoubleUtils.sum(totalNuls, value);
    }

    public void addLocked(double value) {
        lockedNuls = DoubleUtils.sum(lockedNuls, value);
    }

    @Override
    public int compareTo(Object o) {
        if (null == o || !(o instanceof Holder)) {
            return -1;
        }
        Holder obj = (Holder) o;
        if (obj.getTotalNuls() > this.getTotalNuls()) {
            return 1;
        }
        if (obj.getTotalNuls() < this.getTotalNuls()) {
            return -1;
        }
        return 0;
    }
}
