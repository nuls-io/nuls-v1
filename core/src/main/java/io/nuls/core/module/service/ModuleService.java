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
package io.nuls.core.module.service;


import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseModuleBootstrap;
import io.nuls.core.module.manager.ModuleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/10/16
 */
public class ModuleService {

    private ModuleManager moduleManager = ModuleManager.getInstance();

    private static final ModuleService INSTANCE = new ModuleService();

    private ModuleService() {
    }

    public static ModuleService getInstance() {
        return INSTANCE;
    }

    public Short getModuleId(Class<? extends BaseModuleBootstrap> clazz) {
        return moduleManager.getModuleId(clazz);
    }

    public void startModule(String key, String moduleClass) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        moduleManager.startModule(key, moduleClass);
    }

    public ModuleStatusEnum getModuleState(short moduleId) {
        return moduleManager.getModuleState(moduleId);
    }

    public void shutdown(short moduleId) {
        moduleManager.stopModule(moduleId);
    }

    public void destroy(short moduleId) {
        moduleManager.destroyModule(moduleId);
    }

    public void startModules(Map<String, String> bootstrapClasses) {
        moduleManager.setModulesCfg(bootstrapClasses);
        List<String> keyList = new ArrayList<>(bootstrapClasses.keySet());
        for (String key : keyList) {
            try {
                startModule(key, bootstrapClasses.get(key));
            } catch (Exception e) {
                throw new NulsRuntimeException(e);
            }
        }
    }

    public BaseModuleBootstrap getModule(short moduleId) {
        return moduleManager.getModule(moduleId);
    }
}
