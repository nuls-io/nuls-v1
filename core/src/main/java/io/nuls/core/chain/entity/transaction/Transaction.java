package io.nuls.core.chain.entity.transaction;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.crypto.Sha256Hash;

import java.util.List;

/**
 * Created by win10 on 2017/10/30.
 */
public abstract class Transaction extends NulsData {
    public Transaction(){
    }
    //tx type
    protected int type;
    //tx hash
    protected Sha256Hash hash;
    //current time (ms)
    protected long time;
    protected byte[] remark;

    public Sha256Hash getHash() {
        return hash;
    }

    public void setHash(Sha256Hash hash) {
        this.hash = hash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getRemark() {
        return remark;
    }

    public void setRemark(byte[] remark) {
        this.remark = remark;
    }
}
