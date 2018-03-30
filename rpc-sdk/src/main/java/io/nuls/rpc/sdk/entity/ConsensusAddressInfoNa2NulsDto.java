package io.nuls.rpc.sdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/28
 */
public class ConsensusAddressInfoNa2NulsDto extends ConsensusAddressInfoDto {
    public ConsensusAddressInfoNa2NulsDto(Map<String, Object> map) {
        super(map);
    }

    @JsonProperty("totalDeposit")
    public String getTotalDeposit2Nuls(){
        return Na.valueOf(getTotalDeposit()).toText();
    }

    @JsonProperty("usableBalance")
    public String getUsableBalance2Nuls(){
        return Na.valueOf( getUsableBalance()).toText();
    }
}
