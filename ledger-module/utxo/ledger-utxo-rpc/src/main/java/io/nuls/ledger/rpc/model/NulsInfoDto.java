package io.nuls.ledger.rpc.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/10/11
 */
public class NulsInfoDto {

    @ApiModelProperty(name = "totalNuls", value = "总的nuls数量")
    private Long totalNuls;

    @ApiModelProperty(name = "totalNuls", value = "总的nuls数量")
    private Long lockedNuls;

    public Long getTotalNuls() {
        return totalNuls;
    }

    public void setTotalNuls(Long totalNuls) {
        this.totalNuls = totalNuls;
    }

    public Long getLockedNuls() {
        return lockedNuls;
    }

    public void setLockedNuls(Long lockedNuls) {
        this.lockedNuls = lockedNuls;
    }
}
