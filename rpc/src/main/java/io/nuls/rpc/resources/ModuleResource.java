package io.nuls.rpc.resources;


import io.nuls.rpc.entity.RpcResult;

/**
 * @author Niels
 * @date 2017/10/16
 */
public interface ModuleResource extends SystemResource{

    /**
     * start a new module
     * @param moduleName
     * @param moduleClass
     * @return
     */
    RpcResult startModule(String moduleName,String moduleClass) ;

    RpcResult shutdownModule(Short moduleId);

    RpcResult distroyModule(Short moduleId);

    RpcResult restartModule(Short moduleId);
}
