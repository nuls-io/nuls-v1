package io.nuls.core.utils.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    public static void main(String[] args) {
        Data dataProxy = AopUtils.createProxy(Data.class, new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                return proxy.invokeSuper(obj,args);
            }
        });
        Data newData = new Data();
        newData .setVal("100");
        Field h = null;
        try {
            h = dataProxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        h.setAccessible(true);
        try {
           Object obj =  h.get(dataProxy);
            System.out.println(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        h.setAccessible(false);



        System.out.println(dataProxy.getVal());
    }
}
