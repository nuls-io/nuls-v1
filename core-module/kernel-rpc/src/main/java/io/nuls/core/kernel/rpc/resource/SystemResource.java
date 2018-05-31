/*
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
 *
 */

package io.nuls.core.kernel.rpc.resource;

import io.nuls.core.kernel.rpc.model.VersionDto;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.i18n.I18nUtils;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author: Niels Wang
 * @date: 2018/5/22
 */
@Path("/sys")
@Api(value = "/sys", description = "System")
@Component
public class SystemResource {

    @PUT
    @Path("/lang/{language}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "设置系统语言")
    public RpcClientResult setLanguage(@PathParam("language") String language) {
        AssertUtil.canNotEmpty(language);
        boolean b = I18nUtils.hasLanguage(language);
        if(!b){
            return Result.getFailed(KernelErrorCode.DATA_ERROR,"language unkown!").toRpcClientResult();
        }
        try {
            I18nUtils.setLanguage(language);
        } catch (NulsException e) {
            Log.error(e);
            Result.getFailed(e.getMessage());
        }
        return Result.getSuccess().toRpcClientResult();
    }

    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询系统版本信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success",response = VersionDto.class)
    })
    public RpcClientResult getVersion() {
        VersionDto rpcVersion = new VersionDto();
        rpcVersion.setMyVersion(NulsConfig.VERSION);
        rpcVersion.setNewestVersion(NulsConfig.NEWEST_VERSION);
        if(StringUtils.isBlank(NulsConfig.NEWEST_VERSION)){
            rpcVersion.setNewestVersion(NulsConfig.VERSION);
        }
        return Result.getSuccess().setData(rpcVersion).toRpcClientResult();
    }

}
