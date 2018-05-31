package io.nuls.accout.ledger.rpc.dto;

import io.nuls.account.ledger.model.TransactionInfo;

public class TransactionInfoDto {

    private String txHash;

    private long blockHeight;

    private long time;

    private int txType;

    private byte status;

    private String info;

    public TransactionInfoDto() {

    }

    public TransactionInfoDto(TransactionInfo info) {
        this.txHash = info.getTxHash().getDigestHex();
        this.blockHeight = info.getBlockHeight();
        this.time = info.getTime();
        this.status = info.getStatus();
        this.txType = info.getTxType();
        this.info = info.getInfo();
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
