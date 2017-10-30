package io.nuls.core.utils.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Niels on 2017/10/13.
 * nuls.io
 */
public class AopUtils {

    public static final <T> T createProxy(Class<T> clazz,MethodInterceptor interceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new NulsMethodInterceptor(interceptor));

        return (T) enhancer.create();
    }

    public static final <T> T createProxy(Class<T> clazz,Class[] paramsClass,Object[] params,MethodInterceptor interceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new NulsMethodInterceptor(interceptor));
        return (T) enhancer.create(paramsClass,params);
    }

}
