package io.nuls.rpc.aop;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.rpc.entity.RpcResult;
import org.glassfish.grizzly.http.server.Request;

import javax.inject.Inject;
import javax.inject.Provider;
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

    @Inject
    private Provider<Request> grizzlyRequestProvider;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        HttpContextHelper.put(grizzlyRequestProvider.get());
        if (!whiteSheetVerifier(grizzlyRequestProvider.get())) {
            throw new NulsRuntimeException(ErrorCode.REQUEST_DENIED);
        }
        requestContext.setProperty("start", System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Log.info("url:{},IP:{},start:{},end:{},params:{},result:{}", requestContext.getUriInfo().getRequestUri().getPath()+"?"+requestContext.getUriInfo().getRequestUri().getQuery(), grizzlyRequestProvider.get().getRemoteHost()
        ,requestContext.getProperty("start"),System.currentTimeMillis(),null,null);
    }

    @Override
    public Response toResponse(Exception e) {
        RpcResult result = RpcResult.getFailed().setData(e.getMessage());
        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }

    private boolean whiteSheetVerifier(Request request) {
        //TODO
//        System.out.println(request.getRemoteAddr());
//        System.out.println(request.getRemoteHost());
//        System.out.println(request.getRemotePort());
//        System.out.println(request.getRemoteUser());
        return true;
    }
}
