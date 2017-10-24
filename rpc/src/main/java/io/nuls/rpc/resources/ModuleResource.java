package io.nuls.rpc.resources;


import io.nuls.rpc.entity.RpcResult;

/**
 * Created by Niels on 2017/10/16.
 * nuls.io
 */
public interface ModuleResource {

    RpcResult loadModule(String moduleClass) throws IllegalAccessException, InstantiationException, ClassNotFoundException;

    RpcResult startModule(String moduleName);

    RpcResult shutdownModule(String moduleName);

    RpcResult distroyModule(String moduleName);

    RpcResult restartModule(String moduleName);
}
