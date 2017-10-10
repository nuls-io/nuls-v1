package io.nuls.rpcserver.aop;

import io.nuls.exception.NulsRuntimeException;
import io.nuls.rpcserver.entity.RpcResult;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.IOException;

import io.nuls.util.constant.ErrorCode;
import org.glassfish.grizzly.http.server.Request;

/**
 * Created by Niels on 2017/9/28.
 * nuls.io
 */
public class RpcServerFilter implements ContainerRequestFilter,ContainerResponseFilter, ExceptionMapper<Exception>
{

    @Inject
    private Provider<Request> grizzlyRequestProvider;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        HttpContextHelper.put(grizzlyRequestProvider.get());
        if (!whiteSheetVerifier(grizzlyRequestProvider.get())) {
            throw new NulsRuntimeException(ErrorCode.REQUEST_DENIED);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        System.out.println(requestContext);
    }

    @Override
    public Response toResponse(Exception e) {
        RpcResult result = RpcResult.getFailed().setData(e.getMessage());
        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }

    private boolean whiteSheetVerifier(Request request) {
        System.out.println(request.getRemoteAddr());
        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemotePort());
        System.out.println(request.getRemoteUser());
        return true;
    }
}
