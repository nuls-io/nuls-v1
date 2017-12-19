package io.nuls.consensus.entity.params;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.param.AssertUtil;

import java.util.Map;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class JoinConsensusParam {
    public static final String IS_SEED_PEER = "is-seed-peer";
    public static final String DEPOSIT = "deposit";
    public static final String AGENT_ADDRESS = "agentAddress";
    public static final String INTRODUCTION = "introduction";

    private final Map<String, Object> params;

    public JoinConsensusParam(Map<String, Object> map) {
        AssertUtil.canNotEmpty(map, ErrorCode.NULL_PARAMETER.getMsg());
        AssertUtil.canNotEmpty(map.get(DEPOSIT), ErrorCode.NULL_PARAMETER.getMsg());
        AssertUtil.canNotEmpty(map.get(AGENT_ADDRESS), ErrorCode.NULL_PARAMETER.getMsg());
        this.params = map;
    }

    public Boolean isSeed(){
        return (Boolean)params.get(IS_SEED_PEER);
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

}
