package io.nuls.rpc.sdk.entity;

import io.nuls.rpc.sdk.utils.StringUtils;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/25
 */
public class ConsensusAgentInfoDto {

    private String agentAddress;

    private String agentName;

    private Integer status;

    private Long owndeposit;

    private Long totalDeposit;

    private Double commissionRate;

    private Double creditRatio;

    private Integer memberCount;

    private String introduction;

    private Long startTime;

    private Integer packedCount;

    private Long reward;

    public ConsensusAgentInfoDto(Map<String, Object> map){
        this.agentAddress = (String)map.get("agentAddress");
        this.agentName = (String)map.get("agentName");
        this.status = (Integer)map.get("status");
        this.owndeposit = StringUtils.parseLong(map.get("owndeposit"));
        this.totalDeposit = StringUtils.parseLong(map.get("totalDeposit"));
        this.commissionRate = (Double)map.get("commissionRate");
        this.creditRatio = (Double)map.get("creditRatio");
        this.memberCount = (Integer)map.get("memberCount");
        this.introduction = (String)map.get("introduction");
        this.startTime = StringUtils.parseLong(map.get("startTime"));
        this.packedCount = (Integer)map.get("packedCount");
        this.reward = StringUtils.parseLong(map.get("reward"));
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

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Integer getPackedCount() {
        return packedCount;
    }

    public void setPackedCount(Integer packedCount) {
        this.packedCount = packedCount;
    }

    public Long getReward() {
        return reward;
    }

    public void setReward(Long reward) {
        this.reward = reward;
    }
}
