package io.nuls.rpc.sdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date:
 */
public class BlockNa2NulsDto extends BlockDto {
    public BlockNa2NulsDto(Map<String, Object> map, boolean all) {
        super(map, all);
    }

    @JsonProperty("reward")
    public String getReward0() {
        return Na.valueOf(getReward()).toText();
    }

    @JsonProperty("fee")
    public String getFee0() {
        return Na.valueOf(getFee()).toText();
    }
}
