package io.nuls.rpc.sdk.service;

import io.nuls.rpc.sdk.constant.RpcCmdConstant;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.entity.TransactionDto;
import io.nuls.rpc.sdk.entity.TransactionNa2NulsDto;
import io.nuls.rpc.sdk.utils.AssertUtil;
import io.nuls.rpc.sdk.utils.RestFulUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/25
 */
public enum TransactionService {
    TRANSACTION_SERVICE;

    private RestFulUtils restFul = RestFulUtils.getInstance();

    private RpcClientResult getTxBase(String hash){
        try {
            AssertUtil.canNotEmpty(hash);
        } catch (Exception e) {
            return RpcClientResult.getFailed(RpcCmdConstant.PARAMETER_ERROR_MSG);
        }
        RpcClientResult result = restFul.get("/tx/hash/" + hash, null);
        return result;
    }


    public RpcClientResult getTx(String hash){
        RpcClientResult result = getTxBase(hash);
        if(result.isSuccess()){
            result.setData(new TransactionDto((Map<String, Object>)result.getData()));
        }
        return result;
    }

    public RpcClientResult getTxNa2Nuls(String hash){
        RpcClientResult result = getTxBase(hash);
        if(result.isSuccess()){
            result.setData(new TransactionNa2NulsDto((Map<String, Object>)result.getData()));
        }
        return result;
    }

    private RpcClientResult getTxListBase(String address){
        try {
            AssertUtil.canNotEmpty(address);
        } catch (Exception e) {
            return RpcClientResult.getFailed(RpcCmdConstant.PARAMETER_ERROR_MSG);
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("address", String.valueOf(address));
        RpcClientResult result = restFul.get("/tx/list", params);

        return result;
    }

    public RpcClientResult getTxList(String address){
        RpcClientResult result = getTxListBase(address);
        if(result.isSuccess()){
            Map<String, Object> page = (Map<String, Object>)result.getData();
            List<Map<String, Object>> list = (List<Map<String, Object>>)page.get("list");
            List<TransactionDto> transactionDtoList = new ArrayList<>(10);
            for (Map<String, Object> map : list){
                transactionDtoList.add(new TransactionDto(map));
            }
            result.setData(transactionDtoList);
        }
        return result;
    }

    public RpcClientResult getTxListNa2Nuls(String address){
        RpcClientResult result = getTxListBase(address);
        if(result.isSuccess()){
            Map<String, Object> page = (Map<String, Object>)result.getData();
            List<Map<String, Object>> list = (List<Map<String, Object>>)page.get("list");
            List<TransactionDto> transactionDtoList = new ArrayList<>(10);
            for (Map<String, Object> map : list){
                transactionDtoList.add(new TransactionNa2NulsDto(map));
            }
            result.setData(transactionDtoList);
        }
        return result;
    }
}
