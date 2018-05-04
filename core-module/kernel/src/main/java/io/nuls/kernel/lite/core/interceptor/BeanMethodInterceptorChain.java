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
 * @author Niels Wang
 * @date 2018/1/30
 */
public class BeanMethodInterceptorChain {
    protected List<BeanMethodInterceptor> interceptorList = new ArrayList<>();
    private ThreadLocal<Integer> index = new ThreadLocal<>();
    private ThreadLocal<MethodProxy> methodProxyThreadLocal = new ThreadLocal<>();

    protected void add(BeanMethodInterceptor filter) {
        interceptorList.add(filter);
    }

    public Object startFilter(Annotation ann, Object obj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        methodProxyThreadLocal.set(methodProxy);
        index.set(-1);
        Object result = null;
        try {
            result = execute(ann, obj, method, params);
        } finally {
            index.remove();
            methodProxyThreadLocal.remove();
        }
        return result;
    }


    public Object execute(Annotation ann, Object obj, Method method, Object[] params) throws Throwable {
        index.set(1 + index.get());
        if (index.get() == interceptorList.size()) {
            return methodProxyThreadLocal.get().invokeSuper(obj, params);
        }
        BeanMethodInterceptor interceptor = interceptorList.get(index.get());
        return interceptor.intercept(ann, obj, method, params, this);
    }

}
