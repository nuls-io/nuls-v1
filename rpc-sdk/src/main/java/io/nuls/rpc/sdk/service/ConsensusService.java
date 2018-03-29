package io.nuls.rpc.sdk.service;

import io.nuls.rpc.sdk.entity.*;
import io.nuls.rpc.sdk.params.CreateAgentParams;
import io.nuls.rpc.sdk.params.WithdrawParams;
import io.nuls.rpc.sdk.params.DepositParams;
import io.nuls.rpc.sdk.params.StopAgentParams;
import io.nuls.rpc.sdk.utils.AssertUtil;
import io.nuls.rpc.sdk.utils.JSONUtils;
import io.nuls.rpc.sdk.utils.RestFulUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/03/25
 */
public enum ConsensusService {
    CONSENSUS_SERVICE;

    private RestFulUtils restFul = RestFulUtils.getInstance();

    public RpcClientResult getConsensus(){
        RpcClientResult result = restFul.get("/consensus", null);
        if(result.isSuccess()){
            result.setData(new ConsensusIntegratedDto((Map<String, Object>)result.getData()));
        }
        return result;
    }
    public RpcClientResult getConsensusNa2Nuls(){
        RpcClientResult result = restFul.get("/consensus", null);
        if(result.isSuccess()){
            result.setData(new ConsensusIntegratedNa2NulsDto((Map<String, Object>)result.getData()));
        }
        return result;
    }

    private RpcClientResult getConsensusAddressBase(String address){
        try {
            AssertUtil.canNotEmpty(address);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.get("/consensus/address/" + address, null);
    }

    public RpcClientResult getConsensusAddress(String address){
        RpcClientResult result = getConsensusAddressBase(address);
        if(result.isSuccess()){
            result.setData(new ConsensusAddressInfoDto((Map<String, Object>)result.getData()));
        }
        return result;
    }

    public RpcClientResult getConsensusAddressNa2Nuls(String address){
        RpcClientResult result = getConsensusAddressBase(address);
        if(result.isSuccess()){
            result.setData(new ConsensusAddressInfoNa2NulsDto((Map<String, Object>)result.getData()));
        }
        return result;
    }


    public RpcClientResult agent(CreateAgentParams params) {
        try {
            AssertUtil.canNotEmpty(params.getAgentAddress());
            AssertUtil.canNotEmpty(params.getAgentName());
            AssertUtil.canNotEmpty(params.getCommissionRate());
            AssertUtil.canNotEmpty(params.getDeposit());
            AssertUtil.canNotEmpty(params.getPackingAddress());
            AssertUtil.canNotEmpty(params.getPassword());
            AssertUtil.canNotEmpty(params.getRemark());
            return restFul.post("/consensus/agent", JSONUtils.obj2json(params));
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

    public RpcClientResult stopAgent (StopAgentParams params) {
        try {
            AssertUtil.canNotEmpty(params.getAddress());
            AssertUtil.canNotEmpty(params.getPassword());
            return restFul.post("/consensus/agent/stop", JSONUtils.obj2json(params));
        } catch (Exception e) {
            e.printStackTrace();
            return RpcClientResult.getFailed(e.getMessage());
        }
    }

    private RpcClientResult getAgentBase(String agentAddress) {
        try {
            AssertUtil.canNotEmpty(agentAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.get("/consensus/agent/" + agentAddress, null);
    }

    public RpcClientResult getAgent(String agentAddress) {
        RpcClientResult result = getAgentBase(agentAddress);
        if(result.isSuccess()){
            result.setData(new ConsensusAgentInfoDto((Map<String, Object>)result.getData()));
        }
        return result;
    }

    public RpcClientResult getAgentNa2Nuls(String agentAddress) {
        RpcClientResult result = getAgentBase(agentAddress);
        if(result.isSuccess()){
            result.setData(new ConsensusAgentInfoNa2NulsDto((Map<String, Object>)result.getData()));
        }
        return result;
    }

    public RpcClientResult getAgentStatus() {
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

    public RpcClientResult getAllAgent() {
        RpcClientResult result = restFul.get("/consensus/agent/list" , null);
        if(result.isSuccess()){
            Map<String, Object> page = (Map<String, Object>)result.getData();
            List<Map<String, Object>> list = (List<Map<String, Object>>)page.get("list");
            List<ConsensusAgentListDto> consensusAgentListDtoList = new ArrayList<>(10);
            for (Map<String, Object> map : list){
                consensusAgentListDtoList.add(new ConsensusAgentListDto(map));
            }
            result.setData(consensusAgentListDtoList);
        }
        return result;
    }

}
