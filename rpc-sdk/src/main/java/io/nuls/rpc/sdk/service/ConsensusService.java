package io.nuls.rpc.sdk.service;

import io.nuls.rpc.sdk.entity.*;
import io.nuls.rpc.sdk.params.CreateAgentParams;
import io.nuls.rpc.sdk.params.WithdrawParams;
import io.nuls.rpc.sdk.params.DepositParams;
import io.nuls.rpc.sdk.params.StopAgentParams;
import io.nuls.rpc.sdk.utils.AssertUtil;
import io.nuls.rpc.sdk.utils.JSONUtils;
import io.nuls.rpc.sdk.utils.RestFulUtils;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/03/25
 */
public enum ConsensusService {
    CONSENSUS_SERVICE;

    private RestFulUtils restFul = RestFulUtils.getInstance();

    public RpcClientResult getconsensus(){
        RpcClientResult result = restFul.get("/consensus", null);
        if(result.isSuccess()){
            result.setData(new ConsensusIntegratedDto((Map<String, Object>)result.getData()));
        }
        return result;
    }

    public RpcClientResult getconsensusaddress(String address){
        try {
            AssertUtil.canNotEmpty(address);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        RpcClientResult result = restFul.get("/consensus/address/" + address, null);
        if(result.isSuccess()){
            result.setData(new ConsensusAddressInfoDto((Map<String, Object>)result.getData()));
        }
        return result;
    }


    public RpcClientResult createAgent(CreateAgentParams params) {
        try {
            AssertUtil.canNotEmpty(params.getAddress());
            AssertUtil.canNotEmpty(params.getAgentName());
            AssertUtil.canNotEmpty(params.getCommissionRate());
            AssertUtil.canNotEmpty(params.getDeposit());
            AssertUtil.canNotEmpty(params.getPackingAddress());
            AssertUtil.canNotEmpty(params.getPassword());
            AssertUtil.canNotEmpty(params.getRemark());
            return restFul.post("/consensus/createAgent", JSONUtils.obj2json(params));
        } catch (Exception e) {
            e.printStackTrace();
            return RpcClientResult.getFailed(e.getMessage());
        }
    }


    public RpcClientResult deposit(DepositParams params) {
        try {
            AssertUtil.canNotEmpty(params.getAddress());
            AssertUtil.canNotEmpty(params.getDeposit());
            AssertUtil.canNotEmpty(params.getPassword());
            AssertUtil.canNotEmpty(params.getAgentId());
            return restFul.post("/consensus/deposit", JSONUtils.obj2json(params));
        } catch (Exception e) {
            e.printStackTrace();
            return RpcClientResult.getFailed(e.getMessage());
        }
    }

    public RpcClientResult stopagent (StopAgentParams params) {
        try {
            AssertUtil.canNotEmpty(params.getAddress());
            AssertUtil.canNotEmpty(params.getPassword());
            return restFul.post("/consensus/agent/stop", JSONUtils.obj2json(params));
        } catch (Exception e) {
            e.printStackTrace();
            return RpcClientResult.getFailed(e.getMessage());
        }
    }

    public RpcClientResult getagent(String agentAddress) {
        try {
            AssertUtil.canNotEmpty(agentAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return RpcClientResult.getFailed(e.getMessage());
        }
        RpcClientResult result = restFul.get("/consensus/agent/" + agentAddress, null);
        if(result.isSuccess()){
            result.setData(new ConsensusAgentInfoDto((Map<String, Object>)result.getData()));
        }
        return result;
    }

    public RpcClientResult getagentstatus() {
        RpcClientResult result = restFul.get("/consensus/agent/status" , null);
        if(result.isSuccess()){
            result.setData(new ConsensusStatusDto((Map<String, Object>)result.getData()));
        }
        return result;
    }


    public RpcClientResult withdraw(WithdrawParams params) {
        try {
            AssertUtil.canNotEmpty(params.getTxHash());
            AssertUtil.canNotEmpty(params.getAddress());
            AssertUtil.canNotEmpty(params.getPassword());
            return restFul.post("/consensus/withdraw", JSONUtils.obj2json(params));
        } catch (Exception e) {
            e.printStackTrace();
            return RpcClientResult.getFailed(e.getMessage());
        }
    }



}
