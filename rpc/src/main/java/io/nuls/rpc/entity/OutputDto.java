package io.nuls.rpc.entity;

import io.nuls.ledger.entity.OutPutStatusEnum;
import io.nuls.ledger.entity.UtxoOutput;

public class OutputDto {

    private String txHash;

    private Integer index;

    private String address;

    private Long value;

    private Long createTime;

    private Long lockTime;
    //tx.type
    private Integer type;
    // 0, usable, 1, timeLock, 2, consensusLock 3, spent
    private Integer status;

    public OutputDto(UtxoOutput output) {
        this.txHash = output.getTxHash().getDigestHex();
        this.index = output.getIndex();
        this.address = output.getAddress();
        this.value = output.getValue();
        this.createTime = output.getCreateTime();
        this.lockTime = output.getLockTime();
        this.type = output.getTxType();
        if (OutPutStatusEnum.UTXO_SPENT == output.getStatus()) {
            this.status = 3;
        } else if (OutPutStatusEnum.UTXO_CONFIRMED_CONSENSUS_LOCK == output.getStatus() ||
                OutPutStatusEnum.UTXO_UNCONFIRMED_CONSENSUS_LOCK == output.getStatus()) {
            this.status = 2;
        } else if (OutPutStatusEnum.UTXO_CONFIRMED_TIME_LOCK == output.getStatus() ||
                OutPutStatusEnum.UTXO_UNCONFIRMED_TIME_LOCK == output.getStatus()) {
            this.status = 1;
        } else {
            this.status = 0;
        }

    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getLockTime() {
        return lockTime;
    }

    public void setLockTime(Long lockTime) {
        this.lockTime = lockTime;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
}
