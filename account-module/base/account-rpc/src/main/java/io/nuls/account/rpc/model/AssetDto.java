package io.nuls.account.rpc.model;

import io.nuls.account.model.Balance;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "assetJSON")
public class AssetDto extends BalanceDto {

    @ApiModelProperty(name = "asset", value = "资产名称")
    private String asset;

    public AssetDto(String asset, Balance balance) {
        super(balance);
        this.asset = asset;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }
}
