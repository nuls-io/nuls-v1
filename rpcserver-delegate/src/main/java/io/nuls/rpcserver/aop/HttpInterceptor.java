package io.nuls.rpcserver.aop;

import io.nuls.rpcserver.entity.RpcResult;
import io.nuls.util.constant.ErrorCode;
import io.nuls.util.log.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.glassfish.grizzly.http.server.Request;
import org.springframework.stereotype.Component;


/**
 * Created by Niels on 2017/9/28.
 * nuls.io
 */
@Aspect
@Component
public class HttpInterceptor {

    @Pointcut("execution (* io.nuls.rpcserver.resources.*.*(..))")
    public void aspectJHttpMethod() {
    }

    @Around("aspectJHttpMethod()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result ;
        Object[] args = null;
        do {
            Request request = HttpContextHelper.getRequest();
            if (!whiteSheetVerifier(request)) {
                result = RpcResult.getFailed(ErrorCode.REQUEST_DENIED);
                break;
            }
            args = pjp.getArgs();
            try {
                result = pjp.proceed();
            } catch (Exception e) {
                Log.error(e);
                result = RpcResult.getFailed(ErrorCode.FAILED).setData(e.getMessage());
            }
        } while (false);
        long useTime = System.currentTimeMillis() - start;
        Log.info(pjp.getSignature() + "args:{},return:{},useTime:{}ms", args, result, useTime);
        return result;
    }

    private boolean whiteSheetVerifier(Request request) {
        System.out.println(request.getRemoteAddr());
        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemotePort());
        System.out.println(request.getRemoteUser());
        return true;
    }
}
