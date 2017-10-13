package io.nuls.util.aop;

import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created by Niels on 2017/10/13.
 * nuls.io
 */
public interface NulsMethodFilter {

    void before(Object obj, Method method, Object[] args, MethodProxy methodProxy);

    void after(Object obj, Method method, Object[] args, MethodProxy methodProxy,Object result);

    void exception(Object obj, Method method, Object[] args, MethodProxy methodProxy,Exception e);
}
