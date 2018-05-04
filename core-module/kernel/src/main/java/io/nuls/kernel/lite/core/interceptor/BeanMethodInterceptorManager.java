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
 * @author Niels Wang
 * @date 2018/1/30
 */
public class BeanMethodInterceptorManager {

    private static final Map<Class, BeanMethodInterceptorChain> FILTER_MAP = new HashMap<>();

    public static void addBeanMethodInterceptor(Class annotationType, BeanMethodInterceptor interceptor) {
        BeanMethodInterceptorChain interceptorChain = FILTER_MAP.get(annotationType);
        if (null == interceptorChain) {
            interceptorChain = new BeanMethodInterceptorChain();
        }
        interceptorChain.add(interceptor);
        FILTER_MAP.put(annotationType, interceptorChain);
    }

    public static Object doFilter(Annotation[] anns, Object obj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        List<Annotation> annotations = new ArrayList<>();
        List<BeanMethodInterceptorChain> chainList = new ArrayList<>();
        for(Annotation ann:anns){
            BeanMethodInterceptorChain chain = FILTER_MAP.get(ann.annotationType());
            if(null!=chain){
                chainList.add(chain);
                annotations.add(ann );
            }
        }
        if(annotations.isEmpty()){
            return methodProxy.invokeSuper(obj,params);
        }
        MultipleBeanMethodInterceptorChain chain = new MultipleBeanMethodInterceptorChain(annotations,chainList);
        return chain.startFilter(null,obj,method,params,methodProxy);
    }
}
