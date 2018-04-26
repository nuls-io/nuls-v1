package io.nuls.rpc.sdk.entity;

import io.nuls.rpc.sdk.utils.StringUtils;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/29
 */
public class ConsensusAgentListDto {

    private String agentId;

    private String agentAddress;

    private String agentName;

    private Integer status;

    private Long owndeposit;

    private Long totalDeposit;

    private Double commissionRate;

    private Double creditRatio;

    private Integer memberCount;

    public ConsensusAgentListDto(Map<String, Object> map){
        this.agentId = (String)map.get("agentId");
        this.agentAddress = (String)map.get("agentAddress");
        this.agentName = (String)map.get("agentName");
        this.status = (Integer)map.get("status");
        this.owndeposit = StringUtils.parseLong(map.get("owndeposit"));
        this.totalDeposit = StringUtils.parseLong(map.get("totalDeposit"));
        this.commissionRate =  Double.parseDouble(String.valueOf(map.get("commissionRate")));
        this.creditRatio = Double.parseDouble(String.valueOf(map.get("creditRatio")));
        this.memberCount = (Integer)map.get("memberCount");
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getOwndeposit() {
        return owndeposit;
    }

    public void setOwndeposit(Long owndeposit) {
        this.owndeposit = owndeposit;
    }

    public Long getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(Long totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public Double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(Double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public Double getCreditRatio() {
        return creditRatio;
    }

    public void setCreditRatio(Double creditRatio) {
        this.creditRatio = creditRatio;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }
}
