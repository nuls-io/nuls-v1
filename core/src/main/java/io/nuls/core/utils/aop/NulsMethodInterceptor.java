package io.nuls.core.utils.aop;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created by Niels on 2017/10/13.
 * nuls.io
 */
public final class NulsMethodInterceptor implements MethodInterceptor {

    private MethodInterceptor interceptor;

    public NulsMethodInterceptor(MethodInterceptor interceptor){
        this.interceptor = interceptor;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if(method.isBridge()){
           return methodProxy.invokeSuper(o,objects);
        }
        return interceptor.intercept(o,method,objects,methodProxy);
    }
}
