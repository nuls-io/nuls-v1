package io.nuls.rpc.resources;

import io.nuls.rpc.entity.RpcResult;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
public interface SystemResource {

    RpcResult getVersion();

    RpcResult updateVersion();

}
