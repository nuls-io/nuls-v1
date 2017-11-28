package io.nuls.rpc.resources;


import io.nuls.rpc.entity.RpcResult;

/**
 * Created by Niels on 2017/10/16.
 *
 */
public interface ModuleResource {

    RpcResult startModule(Short moduleId,String moduleClass) throws IllegalAccessException, InstantiationException, ClassNotFoundException;

    RpcResult shutdownModule(Short moduleId);

    RpcResult distroyModule(Short moduleId);

    RpcResult restartModule(Short moduleId);
}
