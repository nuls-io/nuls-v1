package io.nuls.rpc.resources.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.core.module.service.ModuleService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.resources.ModuleResource;
import io.nuls.rpc.resources.SystemResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/sys")
public class SystemResourceImpl implements ModuleResource {
    private NulsContext context = NulsContext.getInstance();

    @Override
    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getVersion() {
        return null;
    }

    @Override
    @PUT
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public io.nuls.rpc.entity.RpcResult updateVersion() {
        return null;
    }

    @POST
    @Path("/module/load")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult startModule(@FormParam("moduleId") Short moduleId, @FormParam("moduleClass") String moduleClass) {
        RpcResult result = null;
        do {
            ModuleService service = context.getService(ModuleService.class);
            AssertUtil.canNotEmpty(service, "System module service error!");
            try {
                service.startModule(moduleId, moduleClass);
            } catch (IllegalAccessException e) {
                Log.error(e);
                break;
            } catch (InstantiationException e) {
                Log.error(e);
                break;
            } catch (ClassNotFoundException e) {
                Log.error(e);
                break;
            }
            result = RpcResult.getSuccess();
        } while (false);
        return result;
    }

    @POST
    @Path("/module/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult shutdownModule(Short moduleId) {
        AssertUtil.canNotEmpty(moduleId, "ModuleName can not empty");

        return RpcResult.getSuccess();
    }

    @POST
    @Path("/module/distroy")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult distroyModule(Short moduleId) {
//        AssertUtil.canNotEmpty(moduleName, "ModuleName can not empty");
//        BaseNulsModule module = context.getModule(moduleName);
//        AssertUtil.canNotEmpty(module, "The module of " + moduleName + " is not exist!");
//        module.destroy();
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/module/restart")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public RpcResult restartModule(Short moduleId) {
//        AssertUtil.canNotEmpty(moduleName, "ModuleName can not empty");
//        BaseNulsModule module = context.getModule(moduleName);
//        AssertUtil.canNotEmpty(module, "The module of " + moduleName + " is not exist!");
//        module.shutdown();
//        module.start();
        return RpcResult.getSuccess();
    }
}
