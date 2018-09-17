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
package io.nuls.kernel.lite.annotation;

import io.nuls.kernel.lite.core.interceptor.BeanMethodInterceptor;

import java.lang.annotation.*;

/**
 * 拦截器注解，标注了该注解的对象，需要实现{@link BeanMethodInterceptor}接口，可以拦截标注了指定注解的方法或对象的全部方法
 * The interceptor annotation, annotated with the object of the annotation, needs to implement the {@link BeanMethodInterceptor} interface,
 * which intercepts all methods or objects that annotate the specified annotation.
 *
 * @author: Niels Wang
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Interceptor {
    /**
     * 该拦截器关心的注解类型,该注解可以标注在类型或者方法上，在类型上时，会拦截该类的所有方法，在方法上时，只拦截标记了的方法,不可以为空
     * The interceptor CARES about the type of annotation, the annotations can be marked on the type or method, on the type,
     * can intercept all the methods of the class, on the way, only intercept marked method, cannot be empty
     *
     * @return Class
     */
    Class value();
}
