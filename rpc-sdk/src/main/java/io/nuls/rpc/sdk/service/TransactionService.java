package io.nuls.rpc.sdk.service;

import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.entity.TransactionDto;
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
public class TransactionService {
    private RestFulUtils restFul = RestFulUtils.getInstance();

    public RpcClientResult getTxByhash(String hash){
        try {
            AssertUtil.canNotEmpty(hash);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        RpcClientResult result = restFul.get("/tx/hash/" + hash, null);
        if(result.isSuccess()){
            result.setData(new TransactionDto((Map<String, Object>)result.getData()));
        }
        return result;
    }

    public RpcClientResult getTxList(String address){
        try {
            AssertUtil.canNotEmpty(address);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("address", String.valueOf(address));
        RpcClientResult result = restFul.get("/tx/list", params);
        if(result.isSuccess()){
            List<Map<String, Object>> list = (List<Map<String, Object>>)result.getData();
            List<TransactionDto> transactionDtoList = new ArrayList<>(10);
            for (Map<String, Object> map : list){
                transactionDtoList.add(new TransactionDto(map));
            }
            result.setData(transactionDtoList);
        }
        return result;
    }
}
