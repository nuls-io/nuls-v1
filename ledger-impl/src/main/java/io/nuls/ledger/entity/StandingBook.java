package io.nuls.ledger.entity;

import io.nuls.account.entity.Account;

/**
 * Created by Niels on 2017/11/13.
 * nuls.io
 */
public class StandingBook {

    private String address;

    private double balance;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
