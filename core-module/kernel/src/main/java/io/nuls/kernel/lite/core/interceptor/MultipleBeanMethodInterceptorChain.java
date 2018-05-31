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

import io.nuls.core.tools.log.Log;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 多重拦截器链:只当一个方法存在多条连接器链时使用，该链每次执行一个方法前初始化、组装
 * Multiple interceptors chain.Only when one method has multiple connector chains,
 * The chain is initialized and assembled every time a method is executed.
 *
 * @author Niels Wang
 * @date 2018/1/30
 */
public class MultipleBeanMethodInterceptorChain extends BeanMethodInterceptorChain {

    /**
     * 注解列表
     */
    protected List<Annotation> annotationList = new ArrayList<>();

    /**
     * 执行进度标记
     * Progress mark
     */
    protected Integer index = -1;

    /**
     * 方法代理器
     * Method proxy object
     */
    protected MethodProxy methodProxy;


    /**
     * 初始化多重拦截器链
     * Initialize multiple interceptor chains.
     *
     * @param annotations 注解列表
     * @param chainList   拦截器链列表
     */
    public MultipleBeanMethodInterceptorChain(List<Annotation> annotations, List<BeanMethodInterceptorChain> chainList) {
        if (null == annotations || annotations.isEmpty()) {
            return;
        }
        for (int i = 0; i < annotations.size(); i++) {
            fillInterceptorList(annotations.get(i), chainList.get(i));
        }
    }

    /**
     * 将一条拦截器链加入到多重拦截器链中
     * Add an interceptor chain to the multiple interceptor chain,
     *
     * @param annotation                 该拦截器链对应的注解、The comment for the interceptor chain.
     * @param beanMethodInterceptorChain 拦截器链
     */
    private void fillInterceptorList(Annotation annotation, BeanMethodInterceptorChain beanMethodInterceptorChain) {
        for (BeanMethodInterceptor interceptor : beanMethodInterceptorChain.interceptorList) {
            annotationList.add(annotation);
            interceptorList.add(interceptor);
        }
    }

    /**
     * 开始执行拦截器链
     * Start executing the interceptor chain.
     *
     * @param annotation  拦截方法的注解实例/Annotation instances of the intercepting method.
     * @param object      方法所属对象/Method owner
     * @param method      方法定义/Method definition
     * @param params      方法参数列表/Method parameter list
     * @param methodProxy 方法代理器
     */
    @Override
    public Object startInterceptor(Annotation annotation, Object object, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        this.methodProxy = methodProxy;
        index = -1;
        Object result = null;
        try {
            result = execute(null, object, method, params);
        } catch (Exception e) {
            Log.error(e);
            throw e;
        } finally {
            index = -1;
            this.methodProxy = null;
        }
        return result;
    }

    /**
     * 调用一个具体的拦截器
     * Call a specific interceptor.
     *
     * @param annotation 拦截方法的注解实例/Annotation instances of the intercepting method.
     * @param object     方法所属对象/Method owner
     * @param method     方法定义/Method definition
     * @param params     方法参数列表/Method parameter list
     * @return 返回拦截的方法的返回值，可以对该值进行处理和替换/Returns the return value of the intercepting method, which can be processed and replaced.
     * @throws Throwable 该方法可能抛出异常，请谨慎处理/This method may throw an exception, handle with care.
     */
    @Override
    public Object execute(Annotation annotation, Object object, Method method, Object[] params) throws Throwable {
        index += 1;
        if (index == interceptorList.size()) {
            return methodProxy.invokeSuper(object, params);
        }
        annotation = annotationList.get(index);
        BeanMethodInterceptor interceptor = interceptorList.get(index);
        return interceptor.intercept(annotation, object, method, params, this);
    }
}
