/**
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
 */
package io.nuls.core.utils.spring.lite.core.interceptor;

import io.nuls.core.utils.log.Log;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels Wang
 * @date 2018/1/30
 */
public class MultipleBeanMethodInterceptorChain extends BeanMethodInterceptorChain {
    protected List<Annotation> annotationList = new ArrayList<>();
    protected Integer index = -1;
    protected MethodProxy methodProxy;


    public MultipleBeanMethodInterceptorChain(List<Annotation> annotations, List<BeanMethodInterceptorChain> chainList) {
        if (null == annotations || annotations.isEmpty()) {
            return;
        }
        for (int i = 0; i < annotations.size(); i++) {
            fillInterceptorList(annotations.get(i), chainList.get(i));
        }
    }

    private void fillInterceptorList(Annotation annotation, BeanMethodInterceptorChain beanMethodInterceptorChain) {
        for (BeanMethodInterceptor interceptor : beanMethodInterceptorChain.interceptorList) {
            annotationList.add(annotation);
            interceptorList.add(interceptor);
        }
    }

    @Override
    public Object startFilter(Annotation ann, Object obj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        this.methodProxy = methodProxy;
        index = -1;
        Object result = null;
        try {
            result = execute(null, obj, method, params);
        } catch (Exception e) {
            Log.error(e);
        } finally {
            index = -1;
            this.methodProxy = null;
        }
        return result;
    }

    @Override
    public Object execute(Annotation ann, Object obj, Method method, Object[] params) throws Throwable {
        index += 1;
        if (index == interceptorList.size()) {
            return methodProxy.invokeSuper(obj, params);
        }
        ann = annotationList.get(index);
        BeanMethodInterceptor interceptor = interceptorList.get(index);
        return interceptor.intercept(ann, obj, method, params, this);
    }
}
