package io.nuls.rpc.sdk.entity;

import io.nuls.rpc.sdk.utils.StringUtils;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/25
 */
public class ConsensusIntegratedDto {

    private Integer agentCount;

    private Long rewardOfDay;

    private Integer consensusAccountNumber;

    private Long totalDeposit;


    public ConsensusIntegratedDto(Map<String, Object> map){
        this.agentCount = (Integer)map.get("agentCount");
        this.rewardOfDay = StringUtils.parseLong(map.get("rewardOfDay"));
        this.consensusAccountNumber = (Integer)map.get("consensusAccountNumber");
        this.totalDeposit = StringUtils.parseLong(map.get("totalDeposit"));
    }

    public int getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

    public long getRewardOfDay() {
        return rewardOfDay;
    }

    public void setRewardOfDay(long rewardOfDay) {
        this.rewardOfDay = rewardOfDay;
    }

    public int getConsensusAccountNumber() {
        return consensusAccountNumber;
    }

    public void setConsensusAccountNumber(int consensusAccountNumber) {
        this.consensusAccountNumber = consensusAccountNumber;
    }

    public long getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(long totalDeposit) {
        this.totalDeposit = totalDeposit;
    }
}
