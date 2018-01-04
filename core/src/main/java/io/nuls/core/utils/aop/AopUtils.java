package io.nuls.core.utils.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * Created by Niels on 2017/10/13.
 *
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

//    public static final<T> T createObjProxy(T obj, MethodInterceptor interceptor) {
//        Enhancer enhancer = new Enhancer();
//        enhancer.setSuperclass(obj.getClass());
//        enhancer.setCallback(new ObjectProxyInterceptor(obj, interceptor));
//        return (T) enhancer.create();
//    }
}
