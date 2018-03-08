package io.nuls.rpc.entity;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.Na;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.entity.UtxoOutput;

public class OutputDto {

    private Integer index;

    private String address;

    private Double value;

    private Long createTime;

    private Long lockTime;
    //tx.type
    private Integer type;

    private Integer status;

    public OutputDto(UtxoOutput output) {
        this.index = output.getIndex();
        this.address = Address.fromHashs(output.getAddress()).getBase58();
        this.value = Na.valueOf(output.getValue()).toDouble();
        this.createTime = output.getCreateTime();
        this.lockTime = output.getLockTime();
        this.type = output.getTxType();
        this.status = output.getStatus();
    }

    public OutputDto(UtxoOutputPo output) {
        this.index = output.getOutIndex();
        this.address = output.getAddress();
        this.value = Na.valueOf(output.getValue()).toDouble();
        this.createTime = output.getCreateTime();
        this.lockTime = output.getLockTime();
        this.type = output.getTxType();
        this.status = output.getStatus().intValue();
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
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
}
