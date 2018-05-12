package io.nuls.account.model;

import io.nuls.core.tools.crypto.Utils;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.TransactionLogicData;
import io.protostuff.Tag;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
public class Alias extends TransactionLogicData {


    private byte[] address;


    private String alias;


    private int status;

    public Alias() {
    }

    public Alias(byte[] address, String alias) {
        this.address = address;
        this.alias = alias;
    }

    public Alias(byte[] address, String alias, int status) {
        this.address = address;
        this.alias = alias;
        this.status = status;
    }

    @Override
    public int size() {
        int s = 0;
        s += Utils.sizeOfBytes(address);
        s += Utils.sizeOfString(alias);
        return s;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public Set<byte[]> getAddresses() {
        Set<byte[]> addressSet = new HashSet<>();
        addressSet.add(this.address);
        return addressSet;
    }
}
