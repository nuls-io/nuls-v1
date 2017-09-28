package io.nuls.rpcserver.aop;

import io.nuls.exception.NulsException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * Created by Niels on 2017/9/28.
 * nuls.io
 */
public class RpcServerFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        HttpContextHelper.put(containerRequestContext.getRequest());
//        throw new NulsException("hello exception ");
    }

}
