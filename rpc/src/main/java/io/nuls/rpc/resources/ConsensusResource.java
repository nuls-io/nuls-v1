package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 *
 * @author Niels
 * @date 2017/9/30
 */
public interface ConsensusResource {
    RpcResult getInfo();

    RpcResult getCondition();

    RpcResult in(String address, String password);

    RpcResult out(String address, String password);
}
