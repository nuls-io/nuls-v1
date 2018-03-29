package io.nuls.rpc.sdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/28
 */
public class ConsensusAgentInfoNa2NulsDto extends ConsensusAgentInfoDto {

    public ConsensusAgentInfoNa2NulsDto(Map<String, Object> map) {
        super(map);
    }

    @JsonProperty("owndeposit")
    public String getOwndeposit2Nuls() {
        return Na.valueOf(getOwndeposit()).toText();
    }

    @JsonProperty("totalDeposit")
    public String getTotalDeposit2Nuls() {
        return Na.valueOf(getTotalDeposit()).toText();
    }

    @JsonProperty("reward")
    public String getReward2Nuls() {
        return Na.valueOf(getReward()).toText();
    }
}
