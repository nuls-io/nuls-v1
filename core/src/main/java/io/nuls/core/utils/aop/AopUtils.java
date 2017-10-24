package io.nuls.core.utils.aop;

import net.sf.cglib.proxy.Enhancer;

/**
 * Created by Niels on 2017/10/13.
 * nuls.io
 */
public class AopUtils {

    public static final <T> T createProxy(Class<T> clazz,NulsMethodFilter filter) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new NulsMethodInterceptor(filter));
        return (T) enhancer.create();
    }

    public static final <T> T createProxy(Class<T> clazz,Class[] paramsClass,Object[] params,NulsMethodFilter filter) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new NulsMethodInterceptor(filter));
        return (T) enhancer.create(paramsClass,params);
    }
}
