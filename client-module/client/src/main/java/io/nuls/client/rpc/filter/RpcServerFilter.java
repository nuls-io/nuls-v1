/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.client.rpc.filter;

import io.nuls.client.rpc.constant.RpcConstant;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.ErrorData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.IOException;

/**
 * @author Niels
 */
public class RpcServerFilter implements ContainerRequestFilter, ContainerResponseFilter, ExceptionMapper<Exception> {

    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;
    private String[] ipArray;
    private boolean all = false;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!whiteSheetVerifier(request)) {
            throw new NulsRuntimeException(KernelErrorCode.REQUEST_DENIED);
        }
        requestContext.setProperty("start", System.currentTimeMillis());

    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
//        Log.info("url:{},IP:{},useTime:{}, params:{},result:{}", requestContext.getUriInfo().getRequestUri().getPath() + "?" + requestContext.getUriInfo().getRequestUri().getQuery(), grizzlyRequestProvider.get().getRemoteAddr()
//                , (System.currentTimeMillis() - Long.parseLong(requestContext.getProperty("start").toString())), null, responseContext.getEntity());
        //todo
        response.setHeader("Access-control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));

    }

    @Override
    public Response toResponse(Exception e) {
//        System.out.println("---------------" + request.getRequestURI());
        Log.error("RequestURI is " + request.getRequestURI(), e);
        RpcClientResult result;
        if (e instanceof NulsException) {
            NulsException exception = (NulsException) e;
            result = new RpcClientResult(false, exception.getErrorCode());
        } else if (e instanceof NulsRuntimeException) {
            NulsRuntimeException exception = (NulsRuntimeException) e;
            result = new RpcClientResult(false, new ErrorData(exception.getCode(), exception.getMessage()));
        } else {
            result = Result.getFailed().setMsg(e.getMessage()).toRpcClientResult();
        }

        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }

    private boolean whiteSheetVerifier(HttpServletRequest request) {
        if (all) {
            return true;
        }
        if (ipArray == null) {
            String ips = null;
            try {
                ips = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_REQUEST_WHITE_SHEET);
            } catch (Exception e) {
                Log.error(e);
            }
            if (StringUtils.isBlank(ips)) {
                return false;
            }
            this.ipArray = ips.split(RpcConstant.WHITE_SHEET_SPLIT);
            for (String ip : ipArray) {
                if ("0.0.0.0".equals(ip)) {
                    this.all = true;
                    return true;
                }
            }
        }
        String realIp = request.getRemoteAddr();
        for (String ip : ipArray) {
            if (ip.equals(realIp)) {
                return true;
            }
        }
        return false;
    }
}
