/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.rpc.resources.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.i18n.I18nUtils;
import io.nuls.core.module.service.ModuleService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.rpc.entity.HelpInfoDto;
import io.nuls.rpc.entity.RpcResult;
import io.nuls.rpc.entity.VersionDto;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author Niels
 * @date 2017/9/30
 */
@Path("/sys")
@Api(value = "/browse", description = "System")
public class SystemResource {

    @GET
    @Path("/lang/{language}")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult setLanguage(@PathParam("language") String language) {
        AssertUtil.canNotEmpty(language);
        boolean b = I18nUtils.hasLanguage(language);
        if(!b){
            return RpcResult.getFailed(ErrorCode.DATA_ERROR,"language unkown!");
        }
        try {
            I18nUtils.setLanguage(language);
        } catch (NulsException e) {
            Log.error(e);
            RpcResult.getFailed(e.getMessage());
        }
        return RpcResult.getSuccess();
    }

    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getVersion() {
        VersionDto rpcVersion = new VersionDto();
        rpcVersion.setMyVersion(NulsContext.VERSION);
        rpcVersion.setNewestVersion(NulsContext.NEWEST_VERSION);
        return RpcResult.getSuccess().setData(rpcVersion);
    }

    @GET
    @Path("/help")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getHelp() {
        HelpInfoDto help = new HelpInfoDto();
        return RpcResult.getSuccess().setData(help);
    }

    @POST
    @Path("/module/load")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult startModule(@FormParam("moduleName") String moduleName, @FormParam("moduleClass") String moduleClass) {
        //todo change the params to a form entity
        RpcResult result = null;
        do {
            ModuleService service = ModuleService.getInstance();
            AssertUtil.canNotEmpty(service, "System module service error!");
            try {
                service.startModule(moduleName, moduleClass);
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
    public RpcResult shutdownModule(Short moduleId) {
        AssertUtil.canNotEmpty(moduleId, "ModuleName can not empty");
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/module/distroy")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult distroyModule(Short moduleId) {
//        AssertUtil.canNotEmpty(moduleName, "ModuleName can not empty");
//        BaseModuleBootstrap module = context.getModule(moduleName);
//        AssertUtil.canNotEmpty(module, "The module of " + moduleName + " is not exist!");
//        module.destroy();
        return RpcResult.getSuccess();
    }

    @POST
    @Path("/module/restart")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult restartModule(Short moduleId) {
//        AssertUtil.canNotEmpty(moduleName, "ModuleName can not empty");
//        BaseModuleBootstrap module = context.getModule(moduleName);
//        AssertUtil.canNotEmpty(module, "The module of " + moduleName + " is not exist!");
//        module.shutdown();
//        module.start();
        return RpcResult.getSuccess();
    }

}
