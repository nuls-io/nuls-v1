package io.nuls.core.thread.manager;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author Niels
 * @date 2017/11/27
 */
public class ThreadPoolInterceiptor implements MethodInterceptor {
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        //todo
        return proxy.invokeSuper(obj,args);
    }
}
