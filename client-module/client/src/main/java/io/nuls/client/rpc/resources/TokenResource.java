package io.nuls.client.rpc.resources;

import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.ledger.rpc.model.TransactionDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author: Charlie
 * @date: 2018/8/22
 */
@Path("/token")
@Api(value = "/token", description = "Token assets statistics")
@Component
public class TokenResource {

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "获取全网Token统计数据")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "success", response = TransactionDto.class)
    })
    public RpcClientResult getTokenStatistical(){

        System.out.println(TimeService.currentTimeMillis());
        return Result.getSuccess().toRpcClientResult();
    }
}
