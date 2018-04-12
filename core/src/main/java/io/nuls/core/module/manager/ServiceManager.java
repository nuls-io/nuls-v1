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
package io.nuls.core.module.manager;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseModuleBootstrap;
import io.nuls.core.module.service.ModuleService;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.spring.lite.core.SpringLiteContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Niels
 * @date 2017/11/28
 */
public class ServiceManager {
    private static final ServiceManager INSTANCE = new ServiceManager();
    private static final Map<Short, Set<Class>> MODULE_INTF_MAP = new ConcurrentHashMap<>();
    private static final Map<Class, Short> MODULE_ID_MAP = new ConcurrentHashMap<>();

    private ServiceManager() {
    }

    public static final ServiceManager getInstance() {
        return INSTANCE;
    }

    public void regService(short moduleId, Class serviceClass) {
        AssertUtil.canNotEmpty(serviceClass, ErrorCode.NULL_PARAMETER);
        if (MODULE_ID_MAP.containsKey(serviceClass)) {
           return;
        }
        SpringLiteContext.putBean(serviceClass);
        Set<Class> set = MODULE_INTF_MAP.get(moduleId);
        if (null == set) {
            set = new HashSet<>();
        }
        set.add(serviceClass);
        MODULE_INTF_MAP.put(moduleId, set);
        MODULE_ID_MAP.put(serviceClass, moduleId);
    }


    public void removeService(short moduleId, Class clazz) {
        MODULE_ID_MAP.remove(clazz);
        Set<Class> set = MODULE_INTF_MAP.get(moduleId);
        if (null != set) {
            set.remove(clazz);
            MODULE_INTF_MAP.put(moduleId, set);
        }
        SpringLiteContext.removeBean(clazz);
    }

    public void removeService(short moduleId) {
        Set<Class> set = MODULE_INTF_MAP.get(moduleId);
        if (null == set) {
            return;
        }
        for (Class clazz : set) {
            removeService(moduleId, clazz);
        }
    }

    public BaseModuleBootstrap getModule(Class clazz) {
        Short moduleId = MODULE_ID_MAP.get(clazz);
        if(moduleId==null){
            return null;
        }
        return ModuleService.getInstance().getModule(moduleId);
    }
}
