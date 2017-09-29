package io.nuls.rpcserver.resources;

import io.nuls.exception.NulsException;
import io.nuls.rpcserver.entity.RpcResult;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Niels on 2017/9/25.
 * nuls.io
 */
@Component
@Path("/")
public class TestResouce {

    public TestResouce() {
    }

    @GET
    @Produces("application/json")
    public RpcResult getMessage0() {
        return RpcResult.getSuccess().setData("hello world");
    }

    @GET
    @Path("/test")
    @Produces("application/json")
    public String getMessage() {
        return RpcResult.getSuccess().setData("hello world222222222222222222").toString();
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public RpcResult getMessage2(String key) {
        if(key==null){
            throw new NulsException("测试抛出异常");
        }
        return RpcResult.getSuccess().setData("hello world3333333333333333333333");
    }
}