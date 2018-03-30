package io.nuls.rpc.sdk.entity;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/25
 */
public class ConsensusStatusDto {

    private String agentAddress;

    private Integer status;

    public ConsensusStatusDto(Map<String, Object> map){
        this.agentAddress = (String)map.get("agentAddress");
        this.status = (Integer) map.get("status");
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
