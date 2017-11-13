package io.nuls.core.chain.entity.transaction;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.utils.io.ByteBuffer;

/**
 * Created by win10 on 2017/10/30.
 */
public abstract class Transaction extends NulsData {

    //tx hash
    protected Sha256Hash hash;
    //交易时间
    protected long time;
    //锁定时间，小于0永久锁定，大于等于0为锁定的时间或者区块高度
    protected long lockTime;
    //交易类型
    protected int type;

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

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
