package io.nuls.core.module.manager;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.module.thread.ModuleThreadPoolExecuter;
import io.nuls.core.utils.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/9/26
 */
public class
ModuleManager {

    private static final ModuleThreadPoolExecuter POOL = ModuleThreadPoolExecuter.getInstance();

    private static final Map<Short, BaseNulsModule> MODULE_MAP = new HashMap<>();

    private static final ModuleManager MANAGER = new ModuleManager();

    private ModuleManager() {
    }

    public static ModuleManager getInstance() {
        return MANAGER;
    }

    public BaseNulsModule getModule(short moduleId) {
        return MODULE_MAP.get(moduleId);
    }

    public Short getModuleId(Class<? extends BaseNulsModule> moduleClass) {
        if (null == moduleClass) {
            return null;
        }

        for (BaseNulsModule module : MODULE_MAP.values()) {
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

    private boolean isImplements(Class superClass, Class<? extends BaseNulsModule> moduleClass) {
        boolean result = moduleClass.equals(superClass);
        if (result) {
            return true;
        }
        if (Object.class.equals(superClass.getSuperclass())) {
            return false;
        }
        return isImplements(superClass.getSuperclass(), moduleClass);
    }

    public void regModule(BaseNulsModule module) {
        short moduleId = module.getModuleId();
        if (MODULE_MAP.keySet().contains(moduleId)) {
            throw new NulsRuntimeException(ErrorCode.THREAD_REPETITION, "the id of Module is already exist(" + module.getModuleName() + ")");
        }
        MODULE_MAP.put(moduleId, module);
    }

    public void remModule(short moduleId) {
        MODULE_MAP.remove(moduleId);
    }


    public void startModule(String key, String moduleClass) {
        POOL.startModule(key,moduleClass);
    }

    public void stopModule(short moduleId) {
        POOL.stopModule(moduleId);
    }

    public void destoryModule(short moduleId) {
        BaseNulsModule module = MODULE_MAP.get(moduleId);
        if (null == module) {
            return;
        }
        module.setStatus(ModuleStatusEnum.DESTROYING);
        try {
            module.shutdown();
            module.destroy();
            POOL.stopModule(module.getModuleId());
            module.setStatus(ModuleStatusEnum.DESTROYED);
        } catch (Exception e) {
            module.setStatus(ModuleStatusEnum.EXCEPTION);
        }
        remModule(module.getModuleId());
        POOL.removeProcess(module.getModuleId());
    }

    public String getInfo() {
        StringBuilder str = new StringBuilder("Message:");
        for (BaseNulsModule module : MODULE_MAP.values()) {
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
        BaseNulsModule module = MODULE_MAP.get(moduleId);
        if (null == module) {
            return ModuleStatusEnum.NOT_FOUND;
        }
        if (ModuleStatusEnum.RUNNING == module.getStatus()) {
            Thread.State state = POOL.getProcessState(moduleId);
            if (state == Thread.State.TERMINATED) {
                module.setStatus(ModuleStatusEnum.EXCEPTION);
            }
        }
        return module.getStatus();
    }

}
