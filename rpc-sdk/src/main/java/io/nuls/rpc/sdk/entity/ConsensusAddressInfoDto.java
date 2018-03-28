package io.nuls.rpc.sdk.entity;

import io.nuls.rpc.sdk.utils.StringUtils;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/25
 */
public class ConsensusAddressInfoDto {

    private Integer agentCount;

    private Long totalDeposit;

    private Long reward;

    private Integer joinAgentCount;

    private Long usableBalance;

    private Long rewardOfDay;

    public ConsensusAddressInfoDto(Map<String, Object> map) {
        this.agentCount = (Integer)map.get("agentCount");
        this.totalDeposit = StringUtils.parseLong(map.get("totalDeposit"));
        this.reward = StringUtils.parseLong(map.get("reward"));
        this.joinAgentCount = (Integer)map.get("joinAgentCount");
        this.usableBalance = StringUtils.parseLong(map.get("usableBalance"));
        this.rewardOfDay = StringUtils.parseLong(map.get("rewardOfDay"));
    }

    public Integer getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(Integer agentCount) {
        this.agentCount = agentCount;
    }

    public Long getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(Long totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public Long getReward() {
        return reward;
    }

    public void setReward(Long reward) {
        this.reward = reward;
    }

    public Integer getJoinAgentCount() {
        return joinAgentCount;
    }

    public void setJoinAgentCount(Integer joinAgentCount) {
        this.joinAgentCount = joinAgentCount;
    }

    public Long getUsableBalance() {
        return usableBalance;
    }

    public void setUsableBalance(Long usableBalance) {
        this.usableBalance = usableBalance;
    }

    public Long getRewardOfDay() {
        return rewardOfDay;
    }

    public void setRewardOfDay(Long rewardOfDay) {
        this.rewardOfDay = rewardOfDay;
    }
}
