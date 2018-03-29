package io.nuls.rpc.sdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.nuls.rpc.sdk.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class BalanceNa2NulsDto extends BalanceDto {
    public BalanceNa2NulsDto(Map<String, Object> map) {
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
