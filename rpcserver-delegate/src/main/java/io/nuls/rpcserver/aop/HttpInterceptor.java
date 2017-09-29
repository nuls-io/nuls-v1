package io.nuls.rpcserver.aop;

import io.nuls.util.log.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Request;

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
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable{
        long start = System.currentTimeMillis();
        Request request = HttpContextHelper.getRequest();

        Object returnObj = null;
        do{
        Object[] args=pjp.getArgs();

            returnObj=pjp.proceed();

            long useTime = System.currentTimeMillis()-start;
            Log.info(pjp.getSignature() + "args:{},return:{},useTime:{}ms", args, returnObj, useTime);
        }while(false);

        //核心逻辑
//
//        Log.debug(pjp.getSignature()+"args:{},return:{},useTime:{}ms",args,retval,useTime);
        return returnObj;
    }
}
