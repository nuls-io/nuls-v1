package io.nuls.rpcserver.resources;

import io.nuls.rpcserver.vo.RpcResult;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
public interface SystemResource {

    RpcResult getVersion();

    RpcResult updateVersion();

}
