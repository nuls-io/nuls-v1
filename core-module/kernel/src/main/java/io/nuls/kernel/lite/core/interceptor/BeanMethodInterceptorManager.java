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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拦截器管理器
 * Interceptor manager.
 *
 * @author Niels Wang
 * @date 2018/1/30
 */
public class BeanMethodInterceptorManager {

    /**
     * 拦截器池
     * The interceptor pool
     */
    private static final Map<Class, BeanMethodInterceptorChain> INTERCEPTOR_MAP = new HashMap<>();

    /**
     * 添加方法拦截器到管理器中
     * Add a method interceptor to the manager.
     *
     * @param annotationType 注解类型
     * @param interceptor    拦截器
     */
    public static void addBeanMethodInterceptor(Class annotationType, BeanMethodInterceptor interceptor) {
        BeanMethodInterceptorChain interceptorChain = INTERCEPTOR_MAP.get(annotationType);
        if (null == interceptorChain) {
            interceptorChain = new BeanMethodInterceptorChain();
        }
        interceptorChain.add(interceptor);
        INTERCEPTOR_MAP.put(annotationType, interceptorChain);
    }

    /**
     * 执行一个方法，根据方法的注解组装拦截器链，并放入拦截器链中执行
     * Implement a method that assembles the interceptor chain according to the method's annotations and puts it into the interceptor chain.
     *
     * @param annotations 方法上标注的注解列表/Method annotated list of annotations.
     * @param object      方法所属对象/Method owner
     * @param method      方法定义/Method definition
     * @param params      方法参数列表/Method parameter list
     * @param methodProxy 方法代理器
     * @return 返回拦截的方法的返回值，可以对该值进行处理和替换/Returns the return value of the intercepting method, which can be processed and replaced.
     * @throws Throwable 该方法可能抛出异常，请谨慎处理/This method may throw an exception, handle with care.
     */
    public static Object doInterceptor(Annotation[] annotations, Object object, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        List<Annotation> annotationList = new ArrayList<>();
        List<BeanMethodInterceptorChain> chainList = new ArrayList<>();
        for (Annotation ann : annotations) {
            BeanMethodInterceptorChain chain = INTERCEPTOR_MAP.get(ann.annotationType());
            if (null != chain) {
                chainList.add(chain);
                annotationList.add(ann);
            }
        }
        if (annotationList.isEmpty()) {
            return methodProxy.invokeSuper(object, params);
        }
        MultipleBeanMethodInterceptorChain chain = new MultipleBeanMethodInterceptorChain(annotationList, chainList);
        return chain.startInterceptor(null, object, method, params, methodProxy);
    }
}
