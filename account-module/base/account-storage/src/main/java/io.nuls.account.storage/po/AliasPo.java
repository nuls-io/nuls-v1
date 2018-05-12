package io.nuls.account.storage.po;

import io.nuls.account.model.Alias;
import io.nuls.kernel.model.BaseNulsData;
import io.protostuff.Tag;

/**
 * @author: Charlie
 * @date: 2018/5/11
 */
public class AliasPo extends BaseNulsData {



    private byte[] address;


    private String alias;

    // 0: locked   1:confirm

    private int status;


    public AliasPo(){

    }

    public AliasPo(Alias alias){
        this.address = alias.getAddress();
        this.alias = alias.getAlias().trim();
        this.status = alias.getStatus();

    }

    public Alias toAlias(AliasPo aliasPo){
        return new Alias(aliasPo.address, aliasPo.getAlias().trim(), aliasPo.getStatus());
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias == null ? null : alias.trim();
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}