package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 * Created by Niels on 2017/9/30.
 * nuls.io
 */
public interface ConsensusResource {
    RpcResult getInfo();

    RpcResult getCondition();

    RpcResult in(String address, String password);

    RpcResult out(String address, String password);
}
