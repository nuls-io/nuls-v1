/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 系统对象管理器中使用的拦截器接口，想要拦截某些方法时，需要定义自己的拦截器，实现该接口
 * The interceptor interface used in the system object manager, when you want to intercept some methods,
 * you need to define your own interceptor to implement the interface.
 *
 * @author: Niels Wang
 */
public interface BeanMethodInterceptor {
    /**
     * 拦截器拦截某个方法时，使用该方法，在方法中通过逻辑决定是否继续调用拦截的方法，可以在调用之前和之后做一些业务上的操作
     * When an interceptor intercepts a method, the method is used to logically determine whether the intercepting method is called in the method,
     * and it can do some business operations before and after the call.
     *
     * @param annotation       拦截方法的注解实例/Annotation instances of the intercepting method.
     * @param object           方法所属对象/Method owner
     * @param method           方法定义/Method definition
     * @param params           方法参数列表/Method parameter list
     * @param interceptorChain 拦截器链
     * @return 返回拦截的方法的返回值，可以对该值进行处理和替换/Returns the return value of the intercepting method, which can be processed and replaced.
     * @throws Throwable 该方法可能抛出异常，请谨慎处理/This method may throw an exception, handle with care.
     */
    Object intercept(Annotation annotation, Object object, Method method, Object[] params, BeanMethodInterceptorChain interceptorChain) throws Throwable;
}
