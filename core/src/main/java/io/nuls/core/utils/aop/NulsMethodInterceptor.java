package io.nuls.core.utils.aop;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created by Niels on 2017/10/13.
 * nuls.io
 */
public class NulsMethodInterceptor implements MethodInterceptor {

    private final NulsMethodFilter filter;

    public NulsMethodInterceptor(NulsMethodFilter filter) {
        this.filter = filter;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        filter.before(obj,method,args);
        Object result;
        try{
            result = methodProxy.invokeSuper(obj, args);
        }catch (Exception e){
            filter.exception(obj,method,args,e);
            throw e;
        }
        filter.after(obj,method,args,result);
        return result;
    }
}
