package io.nuls.rpcserver.aop;

import io.nuls.exception.NulsException;
import io.nuls.global.NulsContext;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by Niels on 2017/9/28.
 * nuls.io
 */
@Provider
public class RpcServerExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        Response.ResponseBuilder ResponseBuilder = null;
        if (e instanceof NulsException){
            //截取自定义类型
            NulsException exp = (NulsException) e;
            ResponseBuilder = Response.ok(e.getMessage(), MediaType.APPLICATION_JSON);
        }else {
            ResponseBuilder = Response.ok("hello exception 22222222222", MediaType.APPLICATION_JSON);
        }
        return ResponseBuilder.build();
    }
}