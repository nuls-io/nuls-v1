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

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Interceptor;
import io.nuls.kernel.lite.annotation.MavenInfo;
import io.nuls.kernel.lite.core.interceptor.BeanMethodInterceptor;
import io.nuls.kernel.lite.core.interceptor.BeanMethodInterceptorManager;
import io.nuls.kernel.lite.utils.ScanUtil;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Niels Wang
 * @date 2018/1/30
 */
public class SpringLiteContext {

    private static final Map<String, Object> BEAN_OK_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Object> BEAN_TEMP_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Class> BEAN_TYPE_MAP = new ConcurrentHashMap<>();
    private static final Map<Class, Set<String>> CLASS_NAME_SET_MAP = new ConcurrentHashMap<>();

    private static MethodInterceptor interceptor;

    public static void init(final String packName) {
        init(packName, new DefaultMethodInterceptor());
    }

    public static void init(final String packName, MethodInterceptor interceptor) {
        SpringLiteContext.interceptor = interceptor;
        List<Class> list = ScanUtil.scan(packName);
        list.forEach((Class clazz) -> checkBeanClass(clazz));
        autowireFields();

    }

    private static void autowireFields() {
        Set<String> keySet = new HashSet<>(BEAN_TEMP_MAP.keySet());
        for (String key : keySet) {
            try {
                injectionBeanFields(BEAN_TEMP_MAP.get(key), BEAN_TYPE_MAP.get(key));
                BEAN_OK_MAP.put(key, BEAN_TEMP_MAP.get(key));
                BEAN_TEMP_MAP.remove(key);
            } catch (Exception e) {
                Log.debug(key + " autowire fields failed!");
            }
        }
    }

    private static void injectionBeanFields(Object obj, Class objType) throws Exception {
        Set<Field> fieldSet = getFieldSet(objType);
        for (Field field : fieldSet) {
            injectionBeanField(obj, field);
        }
    }

    private static Set<Field> getFieldSet(Class objType) {
        Set<Field> set = new HashSet<>();
        Field[] fields = objType.getDeclaredFields();
        for (Field field : fields) {
            set.add(field);
        }
        if (!objType.getSuperclass().equals(Object.class)) {
            set.addAll(getFieldSet(objType.getSuperclass()));
        }
        return set;
    }

    private static void injectionBeanField(Object obj, Field field) throws Exception {
        Annotation[] anns = field.getDeclaredAnnotations();
        if (anns == null || anns.length == 0) {
            return;
        }
        Annotation automired = getFromArray(anns, Autowired.class);
        if (null == automired) {
            return;
        }
        String name = ((Autowired) automired).value();
        Object value = null;
        if (null == name || name.trim().length() == 0) {
            Set<String> nameSet = CLASS_NAME_SET_MAP.get(field.getType());
            if (nameSet == null || nameSet.isEmpty()) {
                throw new Exception("Can't find the bean named:" + name);
            } else if (nameSet.size() == 1) {
                name = nameSet.iterator().next();
            } else {
                name = field.getName();
            }
        }
        value = getBean(name);
        if (null == value) {
            throw new Exception("Can't find the bean named:" + name);
        }
        field.setAccessible(true);
        field.set(obj, value);
        field.setAccessible(false);
    }

    private static Object getBean(String name) {
        Object value = BEAN_OK_MAP.get(name);
        if (null == value) {
            value = BEAN_TEMP_MAP.get(name);
        }
        return value;
    }

    private static void checkBeanClass(Class clazz) {
        Annotation[] anns = clazz.getDeclaredAnnotations();
        if (anns == null || anns.length == 0) {
            return;
        }
        Annotation ann = getFromArray(anns, MavenInfo.class);
        String beanName = null;
        if (ann != null) {
            beanName = ((MavenInfo) ann).value();
            if (beanName == null || beanName.trim().length() == 0) {
                beanName = getBeanName(clazz);
            }
            loadBean(beanName, clazz, false);
        }
        Annotation interceptorAnn = getFromArray(anns, Interceptor.class);
        if (null != interceptorAnn) {
            BeanMethodInterceptor interceptor = null;
            try {
                Constructor constructor = clazz.getDeclaredConstructor();
                interceptor = (BeanMethodInterceptor) constructor.newInstance();
            } catch (Exception e) {
                Log.error(e);
                return;
            }
            BeanMethodInterceptorManager.addBeanMethodInterceptor(((Interceptor) interceptorAnn).value(), interceptor);
        }
    }

    private static String getBeanName(Class clazz) {
        String start = clazz.getSimpleName().substring(0, 1).toLowerCase();
        String end = clazz.getSimpleName().substring(1);
        String beanName = start + end;
        if (BEAN_OK_MAP.containsKey(beanName) || BEAN_TEMP_MAP.containsKey(beanName)) {
            beanName = clazz.getName();
        }
        return beanName;
    }

    private static Annotation getFromArray(Annotation[] anns, Class clazz) {
        for (Annotation ann : anns) {
            if (ann.annotationType().equals(clazz)) {
                return ann;
            }
        }
        return null;
    }

    private static void loadBean(String beanName, Class clazz, boolean proxy) {
        if (BEAN_OK_MAP.containsKey(beanName) || BEAN_TEMP_MAP.containsKey(beanName)) {
            Log.error("bean name repetition (" + beanName + "):" + clazz.getName());
            return;
        }
        Object bean = null;
        if (proxy) {
            bean = createProxy(clazz, interceptor);
        } else {
            try {
                bean = clazz.newInstance();
            } catch (InstantiationException e) {
                Log.error(e);
            } catch (IllegalAccessException e) {
                Log.error(e);
            }
        }
        BEAN_TEMP_MAP.put(beanName, bean);
        BEAN_TYPE_MAP.put(beanName, clazz);
        addClassNameMap(clazz, beanName);
    }

    private static Object createProxy(Class clazz, MethodInterceptor interceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(SpringLiteContext.interceptor);
        return enhancer.create();
    }

    private static void addClassNameMap(Class clazz, String beanName) {
        Set<String> nameSet = CLASS_NAME_SET_MAP.get(clazz);
        if (null == nameSet) {
            nameSet = new HashSet<>();
        }
        nameSet.add(beanName);
        CLASS_NAME_SET_MAP.put(clazz, nameSet);
        if (null != clazz.getSuperclass() && !clazz.getSuperclass().equals(Object.class)) {
            addClassNameMap(clazz.getSuperclass(), beanName);
        }
        if (clazz.getInterfaces() != null && clazz.getInterfaces().length > 0) {
            for (Class intfClass : clazz.getInterfaces()) {
                addClassNameMap(intfClass, beanName);
            }
        }
    }

    public static <T> T getBean(Class<T> beanClass) {
        Set<String> nameSet = CLASS_NAME_SET_MAP.get(beanClass);
        if (null == nameSet || nameSet.isEmpty()) {
            throw new RuntimeException("Can't find bean of " + beanClass.getName());
        }
        if (nameSet.size() > 1) {
            throw new RuntimeException("There are " + nameSet.size() + " beans of " + beanClass.getName());
        }
        T value = null;
        String beanName = null;
        for (String name : nameSet) {
            value = (T) BEAN_OK_MAP.get(name);
            beanName = name;
            break;
        }
        if (null == value) {
            value = (T) BEAN_TEMP_MAP.get(beanName);
        }
        return value;
    }

    public static void putBean(Class clazz) {
        loadBean(getBeanName(clazz), clazz, true);
        autowireFields();
    }

    public static void removeBean(Class clazz) {
        Set<String> nameSet = CLASS_NAME_SET_MAP.get(clazz);
        if (null == nameSet || nameSet.isEmpty()) {
            return;
        }
        for (String name : nameSet) {
            BEAN_OK_MAP.remove(name);
            BEAN_TEMP_MAP.remove(name);
            BEAN_TYPE_MAP.remove(name);
        }

    }

    public static boolean checkBeanOk(Object bean) {
        return BEAN_OK_MAP.containsValue(bean);
    }

    public static <T> List<T> getBeanList(Class<T> beanClass) throws Exception {
        Set<String> nameSet = CLASS_NAME_SET_MAP.get(beanClass);
        if (null == nameSet || nameSet.isEmpty()) {
            throw new Exception("Can't find bean of " + beanClass.getName());
        }
        List<T> tlist = new ArrayList<>();
        for (String name : nameSet) {
            T value = (T) BEAN_OK_MAP.get(name);
            if (value == null) {
                value = (T) BEAN_TEMP_MAP.get(name);
            }
            if (null != value) {
                tlist.add(value);
            }
        }
        return tlist;
    }
}
