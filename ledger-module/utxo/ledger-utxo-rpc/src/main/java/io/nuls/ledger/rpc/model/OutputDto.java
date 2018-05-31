package io.nuls.ledger.rpc.model;

import io.nuls.core.tools.crypto.Base58;
import io.nuls.kernel.model.Coin;
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

    @ApiModelProperty(name = "lockTime", value = "锁定时间")
    private Long lockTime;

    @ApiModelProperty(name = "status",
            value = "状态 0:usable(未花费), 1:timeLock(高度锁定), 2:consensusLock(参与共识锁定), 3:spent(已花费)")
    private Integer status;

    public OutputDto(Coin output) {
        this.address = Base58.encode(output.getOwner());
        this.value = output.getNa().getValue();
        this.lockTime = output.getLockTime();
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

    public Long getLockTime() {
        return lockTime;
    }

    public void setLockTime(Long lockTime) {
        this.lockTime = lockTime;
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
