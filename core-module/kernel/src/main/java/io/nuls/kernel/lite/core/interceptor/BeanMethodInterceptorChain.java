/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.kernel.lite.core.interceptor;

import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 方法拦截器链：一个方法可以被多个拦截器拦截，多个拦截器之间顺序的组成了一条拦截器链，没个拦截器可以决定是否继续执行后面拦截器
 * Method the interceptor chain: one method can be more interceptors to intercept,
 * between multiple interceptors sequence formed a chain of interceptors, behind each blocker can decide whether to continue the interceptor
 *
 * @author Niels Wang
 */
public class BeanMethodInterceptorChain {

//    /**
//     * 链中的拦截器列表
//     * List of interceptors in the interceptors chain.
//     */
    protected List<BeanMethodInterceptor> interceptorList = new ArrayList<>();
//
//    /**
//     * 线程安全的执行缓存，用于标记当前执行进度
//     * Thread-safe execution cache to mark the current execution progress.
//     */
    private ThreadLocal<Integer> index = new ThreadLocal<>();

//    /**
//     * 方法代理器缓存，线程安全
//     * Method agent cache, thread safe.
//     */
    private ThreadLocal<MethodProxy> methodProxyThreadLocal = new ThreadLocal<>();

//    /**
//     * 像链中添加一个方法拦截器
//     * Add a method interceptor to the chain.
//     *
//     * @param interceptor 拦截器
//     */
    protected void add(BeanMethodInterceptor interceptor) {
        interceptorList.add(interceptor);
    }

    /**
     * 将一个方法放入该拦截器链中执行，获取返回结果
     * Puts a method in the interceptor chain to retrieve the returned result.
     *
     * @param annotation  拦截方法的注解实例/Annotation instances of the intercepting method.
     * @param object      方法所属对象/Method owner
     * @param method      方法定义/Method definition
     * @param params      方法参数列表/Method parameter list
     * @param methodProxy 方法代理器
     * @return 返回拦截的方法的返回值，可以对该值进行处理和替换/Returns the return value of the intercepting method, which can be processed and replaced.
     * @throws Throwable 该方法可能抛出异常，请谨慎处理/This method may throw an exception, handle with care.
     */
    public Object startInterceptor(Annotation annotation, Object object, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        methodProxyThreadLocal.set(methodProxy);
        index.set(-1);
        Object result = null;
        try {
            result = execute(annotation, object, method, params);
        } finally {
            index.remove();
            methodProxyThreadLocal.remove();
        }
        return result;
    }
//
//    /**
//     * 调用一个具体的拦截器
//     * Call a specific interceptor.
//     */
    public Object execute(Annotation annotation, Object object, Method method, Object[] params) throws Throwable {
        index.set(1 + index.get());
        if (index.get() == interceptorList.size()) {
            return methodProxyThreadLocal.get().invokeSuper(object, params);
        }
        BeanMethodInterceptor interceptor = interceptorList.get(index.get());
        return interceptor.intercept(annotation, object, method, params, this);
    }

}
