package io.nuls.core.module.service;


import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.module.BaseNulsModule;
import io.nuls.core.module.manager.ModuleManager;
import io.nuls.core.module.manager.ServiceManager;

/**
 * @author Niels
 * @date 2017/10/16
 */
public class ModuleService {
    private static final ModuleService INSTANCE = new ModuleService();

    private ModuleService() {
        ServiceManager.getInstance().regService((short) 0, ModuleService.class, this);
    }

    public static ModuleService getInstance() {
        return INSTANCE;
    }

    public Short getModuleId(Class<? extends BaseNulsModule> clazz) {
        return ModuleManager.getInstance().getModuleId(clazz);
    }

    public void startModule(String key, String moduleClass) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        ModuleManager.getInstance().startModule(key, moduleClass);
    }

    public ModuleStatusEnum getModuleState(short moduleId) {
        return ModuleManager.getInstance().getModuleState(moduleId);
    }

    public void shutdown(short moduleId) {
        ModuleManager.getInstance().stopModule(moduleId);
    }

    public void destroy(short moduleId) {
        ModuleManager.getInstance().destoryModule(moduleId);
    }

}
