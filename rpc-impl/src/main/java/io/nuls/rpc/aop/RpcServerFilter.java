package io.nuls.rpc.aop;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.constant.RpcConstant;
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
 *
 * @author Niels
 * @date 2017/9/28
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
        Log.info("url:{},IP:{},useTime:{}, params:{},result:{}", requestContext.getUriInfo().getRequestUri().getPath() + "?" + requestContext.getUriInfo().getRequestUri().getQuery(), grizzlyRequestProvider.get().getRemoteHost()
                , (System.currentTimeMillis() - Long.parseLong(requestContext.getProperty("start").toString())), null, null);
    }

    @Override
    public Response toResponse(Exception e) {
        RpcResult result = RpcResult.getFailed().setData(e.getMessage());
        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }

    private boolean whiteSheetVerifier(Request request) {
        String ips = null;
        try {
            ips = NulsContext.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_REQUEST_WHITE_SHEET);
        } catch (NulsException e) {
            Log.error(e);
        }
        if (StringUtils.isBlank(ips)) {
            return false;
        }
        String[] ipArray = ips.split(RpcConstant.WHITE_SHEET_SPLIT);
        String realIp = request.getRemoteAddr();
        for (String ip : ipArray) {
            if (ip.equals(realIp)) {
                return true;
            }
        }
//        Log.debug(request.getRemoteAddr());
//        Log.debug(request.getRemoteHost());
//        Log.debug(request.getRemotePort());
//        Log.debug(request.getRemoteUser());
        return false;
    }
}
