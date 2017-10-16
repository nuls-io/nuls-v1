package io.nuls.rpcserver.resources;

import io.nuls.global.NulsContext;
import io.nuls.rpcserver.vo.RpcResult;
import io.nuls.task.ModuleService;
import io.nuls.task.NulsModule;
import io.nuls.util.param.AssertUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by Niels on 2017/9/30.
 * nuls.io
 */
@Path("/sys")
public class SystemResourceImpl implements SystemResource, ModuleResource {
    private NulsContext context = NulsContext.getInstance();

    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getVersion() {
        //todo
        return null;
    }

    @PUT
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public io.nuls.rpcserver.vo.RpcResult updateVersion() {
        //todo
        return null;
    }

    @POST
    @Path("/module/load")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult loadModule(String moduleClass) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        RpcResult result = null;
        do {
            ModuleService service = context.getService(ModuleService.class);
            AssertUtil.canNotEmpty(service, "System module service error!");
            NulsModule module = service.loadModule(moduleClass);
            if (null != module) {
                result = RpcResult.getSuccess();
            }
        } while (false);
        return result;
    }

    @POST
    @Path("/module/start")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult startModule(String moduleName) {
        AssertUtil.canNotEmpty(moduleName, "ModuleName can not empty");
        NulsModule module = context.getModule(moduleName);
        AssertUtil.canNotEmpty(module, "The module of " + moduleName + " is not exist!");
        module.start();
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/module/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult shutdownModule(String moduleName) {
        AssertUtil.canNotEmpty(moduleName, "ModuleName can not empty");
        NulsModule module = context.getModule(moduleName);
        AssertUtil.canNotEmpty(module, "The module of " + moduleName + " is not exist!");
        module.shutdown();
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/module/distroy")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult distroyModule(String moduleName) {
        AssertUtil.canNotEmpty(moduleName, "ModuleName can not empty");
        NulsModule module = context.getModule(moduleName);
        AssertUtil.canNotEmpty(module, "The module of " + moduleName + " is not exist!");
        module.destroy();
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/module/restart")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult restartModule(String moduleName) {
        AssertUtil.canNotEmpty(moduleName, "ModuleName can not empty");
        NulsModule module = context.getModule(moduleName);
        AssertUtil.canNotEmpty(module, "The module of " + moduleName + " is not exist!");
        module.shutdown();
        module.start();
        return RpcResult.getSuccess();
    }
}
