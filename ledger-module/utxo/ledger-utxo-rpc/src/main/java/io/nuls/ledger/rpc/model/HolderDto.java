package io.nuls.ledger.rpc.model;

import io.nuls.core.tools.calc.DoubleUtils;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Niels Wang
 * @date: 2018/10/11
 */
public class HolderDto {

    @ApiModelProperty(name = "address", value = "账户地址")
    private String address;

    @ApiModelProperty(name = "totalNuls", value = "账户余额")
    private String totalNuls;

    @ApiModelProperty(name = "lockedNuls", value = "账户锁定余额")
    private String lockedNuls;

    public HolderDto(Holder holder) {
        this.address = holder.getAddress();
        this.totalNuls = DoubleUtils.getRoundStr(holder.getTotalNuls(),8,true);
        this.lockedNuls = DoubleUtils.getRoundStr(holder.getLockedNuls(),8,true);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotalNuls() {
        return totalNuls;
    }

    public void setTotalNuls(String totalNuls) {
        this.totalNuls = totalNuls;
    }

    public String getLockedNuls() {
        return lockedNuls;
    }

    public void setLockedNuls(String lockedNuls) {
        this.lockedNuls = lockedNuls;
    }
}
