package io.nuls.db.entity;

/**
 * Created by Niels on 2017/11/15.
 */
public class LocalAccountPo extends AccountPo{
    private byte[] priKey;
    private byte[] priSeed;

    public byte[] getPriKey() {
        return priKey;
    }

    public void setPriKey(byte[] priKey) {
        this.priKey = priKey;
    }

    public byte[] getPriSeed() {
        return priSeed;
    }

    public void setPriSeed(byte[] priSeed) {
        this.priSeed = priSeed;
    }
}
