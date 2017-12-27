package io.nuls.rpc.resources.impl;

import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.rpc.entity.RpcResult;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Niels
 * @date 2017/9/25
 *
 */
@Path("/")
public class ExampleResource {

    public ExampleResource() {
        Log.debug("haha");
    }

    @GET
    @Produces("application/json")
    public RpcResult getMessage0() {
        return RpcResult.getSuccess().setData("hello world");
    }

    @GET
    @Path("/hello/{key1}")
    @Produces("application/json")
    public String getMessage(@PathParam("key1") String key1          ,@QueryParam("key") String key) {
        return RpcResult.getSuccess().setData("hello world222222222222222222,"+key1+","+key).toString();
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getMessage2(@QueryParam("key") String key) {
        AssertUtil.canNotEmpty(key,"测试抛出异常");
        return RpcResult.getSuccess().setData("hello world3333333333333333333333");
    }
}