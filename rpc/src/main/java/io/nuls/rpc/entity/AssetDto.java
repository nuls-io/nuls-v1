package io.nuls.rpc.entity;

import io.nuls.ledger.entity.Balance;

public class AssetDto extends BalanceDto {

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
