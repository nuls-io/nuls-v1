package io.nuls.rpc.sdk.service;

import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.utils.RestFulUtils;

public class SystemService {

    private static SystemService instance = new SystemService();

    public static SystemService getInstance() {
        return instance;
    }

    private RestFulUtils restFul = RestFulUtils.getInstance();

    public RpcClientResult getVersion() {
        return restFul.get("/sys/version", null);
    }
}
