package io.nuls.rpc.sdk.service;

import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.entity.VersionDto;
import io.nuls.rpc.sdk.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 * @date 2018-03-07
 */
public enum SystemService {
    SYSTEM_SERVICE;

    private RestFulUtils restFul = RestFulUtils.getInstance();

    /**
     * get the version of the nuls client&the newest version from network
     *
     * @return
     */
    public RpcClientResult getVersion() {
        RpcClientResult result = restFul.get("/sys/version", null);
        if (result.isSuccess()) {
            result.setData(new VersionDto((Map<String, Object>) result.getData()));
        }
        return result;
    }


    /**
     * start a module
     *
     * @param moduleName
     * @param moduleClass
     * @return
     */
    public RpcClientResult startModule(String moduleName, String moduleClass) {
        Map<String, String> params = new HashMap<>();
        params.put("moduleName", moduleName);
        params.put("moduleClass", moduleClass);
        RpcClientResult result = restFul.get("/sys/module/load", params);
        if (result.isSuccess()) {
            result.setData(new VersionDto((Map<String, Object>) result.getData()));
        }
        return result;
    }


}
