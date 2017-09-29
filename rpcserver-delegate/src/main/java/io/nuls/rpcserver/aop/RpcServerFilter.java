package io.nuls.rpcserver.aop;

import io.nuls.rpcserver.entity.RpcResult;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.IOException;

/**
 * Created by Niels on 2017/9/28.
 * nuls.io
 */
public class RpcServerFilter implements ContainerRequestFilter, ContainerResponseFilter, ExceptionMapper<Exception> {
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        HttpContextHelper.put(containerRequestContext.getRequest());
//        throw new NulsException("hello exception ");
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
//        System.out.println(containerRequestContext);
    }

    @Override
    public Response toResponse(Exception e) {
        RpcResult result = RpcResult.getFailed().setData(e.getMessage());
        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }
}
