package io.nuls.contract.vm.program;

import java.math.BigInteger;

public class ProgramAccount {

    private byte[] address;

    private BigInteger balance;

    public ProgramAccount(byte[] address, BigInteger balance) {
        this.address = address;
        this.balance = balance;
    }

    public byte[] getAddress() {
        return address;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public BigInteger addBalance(BigInteger value) {
        balance = balance.add(value);
        return balance;
    }

}
