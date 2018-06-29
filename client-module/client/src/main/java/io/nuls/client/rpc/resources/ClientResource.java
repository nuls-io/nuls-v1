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

package io.nuls.client.rpc.resources;

import io.nuls.client.rpc.RpcServerManager;
import io.nuls.client.rpc.resources.dto.UpgradeProcessDTO;
import io.nuls.client.rpc.resources.dto.VersionDto;
import io.nuls.client.rpc.resources.thread.ShutdownHook;
import io.nuls.client.rpc.resources.thread.UpgradeThread;
import io.nuls.client.version.SyncVersionRunner;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.param.AssertUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.core.tools.str.VersionUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.manager.ConnectionManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.awt.image.Kernel;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Niels Wang
 */
@Path("/client")
@Api(value = "/client", description = "Client")
@Component
public class ClientResource {

    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询系统版本信息")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = VersionDto.class)
    })
    public RpcClientResult getVersion() {
        VersionDto rpcVersion = new VersionDto();
        rpcVersion.setMyVersion(NulsConfig.VERSION);
        SyncVersionRunner syncer = SyncVersionRunner.getInstance();
        rpcVersion.setNewestVersion(syncer.getNewestVersion());
        if (StringUtils.isBlank(rpcVersion.getNewestVersion())) {
            rpcVersion.setNewestVersion(NulsConfig.VERSION);
        }
        rpcVersion.setInfromation(syncer.getInformation());
        boolean upgradable = VersionUtils.higherThan(rpcVersion.getNewestVersion(), NulsConfig.VERSION);
        URL url = ClientResource.class.getClassLoader().getResource("libs");
        upgradable = upgradable && url != null;
        rpcVersion.setUpgradable(upgradable);
        return Result.getSuccess().setData(rpcVersion).toRpcClientResult();
    }

    @GET
    @Path("/upgrade")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "查询升级进度")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = UpgradeProcessDTO.class)
    })
    public RpcClientResult getUpgradeProcess() {
        UpgradeProcessDTO dto = UpgradeThread.getInstance().getProcess();
        return Result.getSuccess().setData(dto).toRpcClientResult();
    }

    @POST
    @Path("/upgrade/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "升级")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Boolean.class)
    })
    public RpcClientResult startUpdate(@PathParam("version") String version) {
        AssertUtil.canNotEmpty(version);
        SyncVersionRunner syncor = SyncVersionRunner.getInstance();
        if (!version.equals(syncor.getNewestVersion())) {
            Result result = Result.getFailed(KernelErrorCode.VERSION_NOT_NEWEST);
            return result.toRpcClientResult();
        }
        URL url = ClientResource.class.getClassLoader().getResource("libs");
        if (null == url) {
            return Result.getFailed(KernelErrorCode.DATA_NOT_FOUND).toRpcClientResult();
        }

        UpgradeThread thread = UpgradeThread.getInstance();
        if (thread.isUpgrading()) {
            return Result.getFailed(KernelErrorCode.UPGRADING).toRpcClientResult();
        }
        boolean result = thread.start();
        if (result) {
            TaskManager.createAndRunThread((short) 1, "upgrade", thread);
            Map<String, Boolean> map = new HashMap<>();
            map.put("value", true);
            return Result.getSuccess().setData(map).toRpcClientResult();
        }
        return Result.getFailed(KernelErrorCode.FAILED).toRpcClientResult();
    }

    @POST
    @Path("/upgrade/stop")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "停止升级")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Boolean.class)
    })
    public RpcClientResult stopUpdate() {
        UpgradeThread thread = UpgradeThread.getInstance();
        if (!thread.isUpgrading()) {
            return Result.getFailed(KernelErrorCode.NOT_UPGRADING).toRpcClientResult();
        }
        if (thread.getProcess().getPercentage() == 100) {
            return Result.getFailed(KernelErrorCode.UPGRADING).toRpcClientResult();
        }
        boolean result = thread.stop();
        if (result) {
            Map<String, Boolean> map = new HashMap<>();
            map.put("value", true);
            return Result.getSuccess().setData(map).toRpcClientResult();
        }
        return Result.getFailed(KernelErrorCode.FAILED).toRpcClientResult();
    }

    @POST
    @Path("/restart")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "重启系统")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = Boolean.class)
    })
    public RpcClientResult restartSystem() {
        URL url = ClientResource.class.getClassLoader().getResource("libs");
        if (url == null) {
            return Result.getFailed(KernelErrorCode.FAILED).toRpcClientResult();
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
                RpcServerManager.getInstance().shutdown();
                ConnectionManager.getInstance().shutdown();

                Runtime.getRuntime().addShutdownHook(new ShutdownHook());
                System.exit(0);

            }
        });
        t.start();
        Map<String, Boolean> map = new HashMap<>();
        map.put("value", true);
        return Result.getSuccess().setData(map).toRpcClientResult();
    }
}
