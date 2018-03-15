package io.nuls.rpc.sdk.service;

import io.nuls.core.utils.log.Log;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.params.CreateAgentParams;
import io.nuls.rpc.sdk.utils.JSONUtils;
import io.nuls.rpc.sdk.utils.RestFulUtils;

/**
 * @author Niels
 * @date 2018-03-07
 */
public class ConsensusService {

    private static ConsensusService instance = new ConsensusService();

    public static ConsensusService getInstance() {
        return instance;
    }

    private RestFulUtils restFul = RestFulUtils.getInstance();

    public RpcClientResult createAgent(CreateAgentParams params) {
        try {
            return restFul.post("/consensus/createAgent", JSONUtils.obj2json(params));
        } catch (Exception e) {
            Log.error(e);
            return RpcClientResult.getFailed(e.getMessage());
        }
    }

    public RpcClientResult entrust() {
        return null;
    }


}
