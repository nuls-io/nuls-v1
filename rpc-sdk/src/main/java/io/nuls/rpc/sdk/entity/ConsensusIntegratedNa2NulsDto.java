package io.nuls.rpc.sdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/28
 */
public class ConsensusIntegratedNa2NulsDto extends ConsensusIntegratedDto {

    public ConsensusIntegratedNa2NulsDto(Map<String, Object> map) {
        super(map);
    }

    @JsonProperty("rewardOfDay")
    public String getRewardOfDay2Nuls() {
        return Na.valueOf(getRewardOfDay()).toText();
    }

    @JsonProperty("totalDeposit")
    public String getTotalDeposit2Nuls() {
        return Na.valueOf(getTotalDeposit()).toText();
    }
}
