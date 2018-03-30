package io.nuls.rpc.sdk.service;

import io.nuls.rpc.sdk.entity.NetworkDto;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.utils.RestFulUtils;

import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/3/26
 */
public enum NetworkService {
    NETWORK_SERVICE;

    private RestFulUtils restFul = RestFulUtils.getInstance();


    public RpcClientResult getnetworkinfo(){
        RpcClientResult result = restFul.get("/network/info", null);
        if(result.isSuccess()){
            result.setData(new NetworkDto((Map<String, Object>)result.getData()));
        }
        return result;
    }

    public RpcClientResult getnetworknodes(){
        return restFul.get("/network/nodes", null);
    }
}
