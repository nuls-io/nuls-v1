package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 * Created by Niels on 2017/10/24.
 *
 */
public interface BroadcastResource {
    RpcResult broadcast(String message);
}
