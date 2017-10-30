package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 * Created by Niels on 2017/10/24.
 * nuls.io
 */
public interface BroadcastResource {
    RpcResult broadcast(String message);
}
