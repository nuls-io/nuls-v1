package io.nuls.accout.ledger.rpc.dto;

import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Transaction;

public class UtxoDto {

    private String txHash;

    private long createTime;

    private int txType;

    private long lockTime;

    private long value;

    public UtxoDto() {

    }

    public UtxoDto(Coin coin, Transaction tx) {
        this.txHash = tx.getHash().getDigestHex();
        this.createTime = tx.getTime();
        this.txType = tx.getType();
        this.lockTime = coin.getLockTime();
        this.value = coin.getNa().getValue();
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
