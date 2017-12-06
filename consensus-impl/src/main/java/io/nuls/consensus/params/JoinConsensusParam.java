package io.nuls.consensus.params;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.param.AssertUtil;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class JoinConsensusParam {

    private static final String DEPOSIT = "deposit";
    private static final String AGENT_ADDRESS = "agentAddress";
    private static final String INTRODUCTION = "introduction";
    private static final String COMMISSION_RATE = "commissionRate";

    private final Map<String, Object> params;

    public JoinConsensusParam(Map<String, Object> map) {
        AssertUtil.canNotEmpty(map, ErrorCode.NULL_PARAMETER.getMsg());
        AssertUtil.canNotEmpty(map.get(DEPOSIT), ErrorCode.NULL_PARAMETER.getMsg());
        AssertUtil.canNotEmpty(map.get(AGENT_ADDRESS), ErrorCode.NULL_PARAMETER.getMsg());
        this.params = map;
    }

    public Double getDeposit() {
        return (Double) params.get(DEPOSIT);
    }

    public String getAgentAddress() {
        return (String) params.get(AGENT_ADDRESS);
    }

    public String getIntroduction() {
        return (String) params.get(INTRODUCTION);
    }

    public Double getCommissionRate() {
        return (Double) params.get(COMMISSION_RATE);
    }
}
