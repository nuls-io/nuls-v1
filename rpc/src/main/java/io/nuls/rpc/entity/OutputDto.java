package io.nuls.rpc.entity;

import io.nuls.ledger.entity.OutPutStatusEnum;
import io.nuls.ledger.entity.UtxoOutput;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "outputJSON")
public class OutputDto {

    @ApiModelProperty(name = "txHash", value = "交易hash")
    private String txHash;

    @ApiModelProperty(name = "index", value = "索引")
    private Integer index;

    @ApiModelProperty(name = "address", value = "地址")
    private String address;

    @ApiModelProperty(name = "value", value = "数量")
    private Long value;

    @ApiModelProperty(name = "createTime", value = "生成时间")
    private Long createTime;

    @ApiModelProperty(name = "lockTime", value = "锁定时间")
    private Long lockTime;

    @ApiModelProperty(name = "type", value = "交易类型")
    private Integer type;

    @ApiModelProperty(name = "status",
            value = "状态 0:usable(未花费), 1:timeLock(高度锁定), 2:consensusLock(参与共识锁定), 3:spent(已花费)")
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
