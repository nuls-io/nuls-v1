package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 *
 * @author Niels
 * @date 2017/10/24
 */
public interface BroadcastResource {
    RpcResult broadcast(String message);
}
