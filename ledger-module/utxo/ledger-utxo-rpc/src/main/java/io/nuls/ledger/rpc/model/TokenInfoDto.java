package io.nuls.ledger.rpc.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/10/11
 */
public class TokenInfoDto {

    @ApiModelProperty(name = "totalNuls", value = "总的nuls数量")
    private String totalNuls;

    @ApiModelProperty(name = "totalNuls", value = "总的nuls数量")
    private String lockedNuls;

    @ApiModelProperty(name = "addressList", value = "持币明细")
    private List<HolderDto> addressList;

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

    public List<HolderDto> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<HolderDto> addressList) {
        this.addressList = addressList;
    }
}
