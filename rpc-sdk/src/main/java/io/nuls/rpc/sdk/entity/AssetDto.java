package io.nuls.rpc.sdk.entity;


import java.util.Map;

public class AssetDto extends BalanceDto {

    private String asset;

    public AssetDto(Map<String, Object> map) {
        super(map);
        this.asset = (String) map.get("asset");
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }
}
