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
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseModuleBootstrap;
import io.nuls.core.module.thread.ModuleProcess;
import io.nuls.core.module.thread.ModuleProcessFactory;
import io.nuls.core.module.thread.ModuleRunner;
import io.nuls.core.utils.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/9/26
 */
public class ModuleManager {

    private Map<String,String> modulesCfg ;

    private final Map<Short, ModuleProcess> PROCCESS_MAP = new HashMap<>();

    private ModuleProcessFactory factory = new ModuleProcessFactory();

    private static final Map<Short, BaseModuleBootstrap> MODULE_MAP = new HashMap<>();

    private static final ModuleManager MANAGER = new ModuleManager();

    private ModuleManager() {
    }

    public static ModuleManager getInstance() {
        return MANAGER;
    }

    public BaseModuleBootstrap getModule(short moduleId) {
        return MODULE_MAP.get(moduleId);
    }

    public Short getModuleId(Class<? extends BaseModuleBootstrap> moduleClass) {
        if (null == moduleClass) {
            return null;
        }

        for (BaseModuleBootstrap module : MODULE_MAP.values()) {
            if (moduleClass.equals(module.getClass()) || isImplements(module.getClass().getSuperclass(), moduleClass)) {
                return module.getModuleId();
            }
        }
        try {
            Thread.sleep(100L);
            Log.warn("wait for the module init:"+moduleClass);
        } catch (InterruptedException e) {
            Log.error(e);
        }
        return this.getModuleId(moduleClass);
    }

    private boolean isImplements(Class superClass, Class<? extends BaseModuleBootstrap> moduleClass) {
        boolean result = moduleClass.equals(superClass);
        if (result) {
            return true;
        }
        if (Object.class.equals(superClass.getSuperclass())) {
            return false;
        }
        return isImplements(superClass.getSuperclass(), moduleClass);
    }

    public void regModule(BaseModuleBootstrap module) {
        short moduleId = module.getModuleId();
        if (MODULE_MAP.keySet().contains(moduleId)) {
            throw new NulsRuntimeException(ErrorCode.THREAD_REPETITION, "the id of Module is already exist(" + module.getModuleName() + ")");
        }
        MODULE_MAP.put(moduleId, module);
    }

    public void remModule(short moduleId) {
        MODULE_MAP.remove(moduleId);
    }


    public void stopModule(short moduleId) {
        BaseModuleBootstrap module = MODULE_MAP.get(moduleId);
        if (null == module) {
            return;
        }
        module.shutdown();
        ModuleProcess process = PROCCESS_MAP.get(moduleId);
        if (null != process && !process.isInterrupted()) {
            process.interrupt();
        }
    }

    public void destroyModule(short moduleId) {
        BaseModuleBootstrap module = MODULE_MAP.get(moduleId);
        if (null == module) {
            return;
        }
        module.setStatus(ModuleStatusEnum.DESTROYING);
        try {
            if(module.getStatus()!=ModuleStatusEnum.STOPED){
                stopModule(moduleId);
            }
            module.destroy();
            remModule(module.getModuleId());
            removeProcess(module.getModuleId());
            module.setStatus(ModuleStatusEnum.DESTROYED);
        } catch (Exception e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
        }

    }

    public String getInfo() {
        StringBuilder str = new StringBuilder("Message:");
        for (BaseModuleBootstrap module : MODULE_MAP.values()) {
            str.append("\nModule:");
            str.append(module.getModuleName());
            str.append("，");
            str.append("id(");
            str.append(module.getModuleId());
            str.append("),");
            str.append("status:");
            str.append(module.getStatus());
            str.append("\nINFO:");
            str.append(module.getInfo());
        }
        return str.toString();
    }

    public ModuleStatusEnum getModuleState(short moduleId) {
        BaseModuleBootstrap module = MODULE_MAP.get(moduleId);
        if (null == module) {
            return ModuleStatusEnum.NOT_FOUND;
        }
        if (ModuleStatusEnum.RUNNING == module.getStatus()) {
            Thread.State state = getProcessState(moduleId);
            if (state == Thread.State.TERMINATED) {
                module.setStatus(ModuleStatusEnum.EXCEPTION);
            }
        }
        return module.getStatus();
    }

    public void startModule(String key, String moduleClass) {
        if(null==moduleClass){
            return;
        }
        try {
            ModuleRunner runner = new ModuleRunner(key, moduleClass);
            ModuleProcess moduleProcess = factory.newThread(runner);
            moduleProcess.start();
        }catch (Exception e){
            Log.error(e);
        }
    }

    private Thread.State getProcessState(short moduleId) {
        ModuleProcess process = PROCCESS_MAP.get(moduleId);
        if (null != process) {
            return process.getState();
        }
        return null;
    }

    public List<ModuleProcess> getProcessList() {
        return new ArrayList<>(PROCCESS_MAP.values());
    }

    private void removeProcess(short moduleId){
        this.destroyModule(moduleId);
        PROCCESS_MAP.remove(moduleId);
    }

    public Map<String, String> getModulesCfg() {
        return modulesCfg;
    }

    public void setModulesCfg(Map<String, String> modulesCfg) {
        this.modulesCfg = modulesCfg;
    }

    public List<BaseModuleBootstrap> getModuleList() {
        return new ArrayList<>(MODULE_MAP.values());
    }
}
