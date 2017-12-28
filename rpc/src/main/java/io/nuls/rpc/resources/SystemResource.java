package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 *
 * @author Niels
 * @date 2017/9/27
 */
public interface SystemResource {

    RpcResult getVersion();

    RpcResult updateVersion();

}
