package io.nuls.rpc.sdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class AssetNa2NulsDto extends AssetDto {
    public AssetNa2NulsDto(Map<String, Object> map) {
        super(map);
    }

    @JsonProperty("balance")
    public String getBalance0() {
        return Na.valueOf(getBalance()).toText();
    }

    @JsonProperty("usable")
    public String getUsable0() {
        return Na.valueOf(getUsable()).toText();
    }

    @JsonProperty("locked")
    public String getLocked0() {
        return Na.valueOf(getLocked()).toText();
    }

}
