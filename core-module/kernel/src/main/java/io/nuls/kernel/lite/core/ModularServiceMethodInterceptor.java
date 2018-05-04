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

package io.nuls.kernel.lite.core;

import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.ModuleStatusEnum;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.lite.core.interceptor.BeanMethodInterceptorManager;
import io.nuls.kernel.lite.exception.BeanStatusException;
import io.nuls.kernel.module.BaseModuleBootstrap;
import io.nuls.kernel.module.manager.ServiceManager;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Niels
 * @date 2018/1/31
 */
public class ModularServiceMethodInterceptor implements MethodInterceptor {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    @Override
    public Object intercept(Object obj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
//        Log.debug(method.toString());
        threadLocal.set(0);
        Throwable throwable = null;
        while (threadLocal.get() < 100) {
            try {
                return this.doIntercept(obj, method, params, methodProxy);
            } catch (BeanStatusException e) {
                threadLocal.set(threadLocal.get() + 1);
                throwable = e;
                Thread.sleep(200L);
            }
        }
        throw throwable;
    }

    private Object doIntercept(Object obj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        List<Annotation> annotationList = new ArrayList<>();
        if (!method.getDeclaringClass().equals(Object.class)) {
            String className = obj.getClass().getCanonicalName();
            className = className.substring(0, className.indexOf("$$"));
            Class clazz = Class.forName(className);
            fillAnnotationList(annotationList, clazz, method);
            BaseModuleBootstrap module = ServiceManager.getInstance().getModule(clazz);
            if (module == null) {
                throw new BeanStatusException(ErrorCode.DATA_ERROR, "Access to a service of an un start module![" + className + "]" + method.toString());
            }
            if (module.getModuleId() != NulsConstant.MODULE_ID_MICROKERNEL &&
                    module.getStatus() != ModuleStatusEnum.STARTING &&
                    module.getStatus() != ModuleStatusEnum.RUNNING) {
                throw new BeanStatusException(ErrorCode.DATA_ERROR, "Access to a service of an un start module![" + module.getModuleName() + "]" + method.toString());
            }
            boolean isOk = SpringLiteContext.checkBeanOk(obj);
            if (!isOk) {
                throw new BeanStatusException(ErrorCode.DATA_ERROR, "Service has not autowired");
            }
        }
        if (annotationList.isEmpty()) {
            return methodProxy.invokeSuper(obj, params);
        }
        return BeanMethodInterceptorManager.doFilter(annotationList.toArray(new Annotation[annotationList.size()]), obj, method, params, methodProxy);

    }

    private void fillAnnotationList(List<Annotation> annotationList, Class clazz, Method method) {
        Set<Class> classSet = new HashSet<>();
        for (Annotation ann : method.getDeclaredAnnotations()) {
            annotationList.add(ann);
            classSet.add(ann.annotationType());
        }
        for (Annotation ann : clazz.getDeclaredAnnotations()) {
            if (classSet.add(ann.annotationType())) {
                annotationList.add(0, ann);
            }
        }
        for (Annotation ann : clazz.getAnnotations()) {
            if (classSet.add(ann.annotationType())) {
                annotationList.add(0, ann);
            }
        }
    }
}
