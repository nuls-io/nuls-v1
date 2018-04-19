package io.nuls.rpc.resources.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author: Niels Wang
 * @date: 2018/3/20
 */
@ApiModel(value = "wholeNetConsensusInfoJSON")
public class WholeNetConsensusInfoDTO {

    @ApiModelProperty(name = "agentCount", value = "节点数量")
    private int agentCount;

    @ApiModelProperty(name = "totalDeposit", value = "总抵押")
    private long totalDeposit;

    @ApiModelProperty(name = "rewardOfDay", value = "24小时共识奖励")
    private long rewardOfDay;

    @ApiModelProperty(name = "consensusAccountNumber", value = "参与共识账户总数量")
    private int consensusAccountNumber;

    public int getConsensusAccountNumber() {
        return consensusAccountNumber;
    }

    public void setConsensusAccountNumber(int consensusAccountNumber) {
        this.consensusAccountNumber = consensusAccountNumber;
    }

    public int getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

    public long getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(long totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public long getRewardOfDay() {
        return rewardOfDay;
    }

    public void setRewardOfDay(long rewardOfDay) {
        this.rewardOfDay = rewardOfDay;
    }
}
