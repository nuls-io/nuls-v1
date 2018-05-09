package io.nuls.account.model;

import io.nuls.kernel.model.BaseNulsData;

/**
 * @author: Charlie
 * @date: 2018/5/9
 */
public class Alias extends BaseNulsData {
    private String address;

    private String alias;

    private int status;

    public Alias() {

    }

    public Alias(String address, String alias) {
        this.address = address;
        this.alias = alias;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
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
}
